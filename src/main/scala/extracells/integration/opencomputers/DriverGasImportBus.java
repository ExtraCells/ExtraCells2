package extracells.integration.opencomputers;

import appeng.api.parts.IPart;
import extracells.part.fluid.PartFluidImport;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import extracells.part.gas.PartGasImport;
import extracells.registries.PartEnum;
import extracells.util.GasUtil;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class DriverGasImportBus extends DriverBase<PartGasImport> {

	public DriverGasImportBus() {
		super(PartEnum.GASIMPORT, Enviroment.class);
	}

	@Override
	protected ManagedEnvironment createEnvironment(IPartHost host) {
		return new Enviroment(host);
	}

	private static PartGasImport getImportBus(World world, BlockPos pos, AEPartLocation dir){
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null || (!(tile instanceof IPartHost)))
			return null;
		IPartHost host = (IPartHost) tile;
		if(dir == null || dir == AEPartLocation.INTERNAL){
			for (AEPartLocation side: AEPartLocation.SIDE_LOCATIONS){
				IPart part = host.getPart(side);
				if (part != null && part instanceof PartGasImport)
					return (PartGasImport) part;
			}
			return null;
		}else{
			IPart part = host.getPart(dir);
			return part == null ? null : part instanceof PartGasImport ? (PartGasImport) part : null;
		}
	}

	public class Enviroment extends AbstractManagedEnvironment implements NamedBlock {

		protected final TileEntity tile;
		protected final IPartHost host;

		public Enviroment(IPartHost host) {
			tile = (TileEntity) host;
			this.host = host;
			setNode(Network.newNode(this, Visibility.Network).
				withComponent("me_importbus").
				create());
		}

		@Callback(doc = "function(side:number, [ slot:number]):table -- Get the configuration of the gas import bus pointing in the specified direction.")
		public Object[] getGasImportConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			PartGasImport part = OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartGasImport.class);
			if (part == null) {
				return new Object[]{null, "no export bus"};
			}
			int slot = args.optInteger(1, 4);
			try {
				Fluid fluid = part.filterFluids[slot];
				if (fluid == null) {
					return new Object[]{null};
				}
				return new Object[]{GasUtil.getGasStack(new FluidStack(fluid, 1000))};
			} catch (Throwable e) {
				return new Object[]{null, "Invalid slot"};
			}

		}

		@Callback(doc = "function(side:number[, slot:number][, database:address, entry:number]):boolean -- Configure the gas import bus pointing in the specified direction to export gas stacks matching the specified descriptor.")
		public Object[] setGasImportConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			PartGasImport part = OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartGasImport.class);
			if (part == null) {
				return new Object[]{null, "no export bus"};
			}
			int slot;
			String address;
			int entry;
			if (args.count() == 3) {
				address = args.checkString(1);
				entry = args.checkInteger(2);
				slot = 4;
			} else if (args.count() < 3) {
				slot = args.optInteger(1, 4);
				try {
					part.filterFluids[slot] = null;
					part.onInventoryChanged();
					context.pause(0.5);
					return new Object[]{true};
				} catch (Throwable e) {
					return new Object[]{false, "invalid slot"};
				}
			} else {
				slot = args.optInteger(1, 4);
				address = args.checkString(2);
				entry = args.checkInteger(3);
			}
			Node node = node().network().node(address);
			if (node == null) {
				throw new IllegalArgumentException("no such component");
			}
			if (!(node instanceof Component)) {
				throw new IllegalArgumentException("no such component");
			}
			Component component = (Component) node;
			Environment env = node.host();
			if (!(env instanceof Database)) {
				throw new IllegalArgumentException("not a database");
			}
			Database database = (Database) env;
			try {
				ItemStack data = database.getStackInSlot(entry - 1);
				if (data == null) {
					part.filterFluids[slot] = null;
				} else {
					GasStack fluid = GasUtil.getGasFromContainer(data);
					if (fluid == null || fluid.getGas() == null) {
						return new Object[]{false, "not a fluid container"};
					}
					part.filterFluids[slot] = GasUtil.getFluidStack(fluid).getFluid();
				}
				part.onInventoryChanged();
				context.pause(0.5);
				return new Object[]{true};
			} catch (Throwable e) {
				return new Object[]{false, "invalid slot"};
			}
		}

		@Callback(doc = "function(side:number, amount:number):boolean -- Make the gas import bus facing the specified direction perform a single import operation.")
		public Object[] importGas(Context context, Arguments args){
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL)
				return new Object[]{false, "unknown side"};
			PartFluidImport part = getImportBus(tile.getWorld(), tile.getPos(), dir);
			if (part == null)
				return new Object[]{false, "no export bus"};
			if (part.getFacingTank() == null)
				return new Object[]{false, "no tank"};
			int amount = Math.min(args.optInteger(1, 625), 125 + part.getSpeedState() * 125);
			boolean didSomething = part.doWork(amount, 1);
			if (didSomething)
				context.pause(0.25);
			return new Object[]{didSomething};
		}

		@Override
		public String preferredName() {
			return "me_importbus";
		}

		@Override
		public int priority() {
			return 1;
		}

	}

}
