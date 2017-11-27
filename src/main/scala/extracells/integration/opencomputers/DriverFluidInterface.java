package extracells.integration.opencomputers;

import li.cil.oc.api.driver.DriverBlock;
import li.cil.oc.api.network.*;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import extracells.api.IFluidInterface;
import extracells.part.fluid.PartFluidInterface;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.FluidHelper;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;

public class DriverFluidInterface implements DriverBlock, EnvironmentProvider {

	@Override
	public boolean worksWith(World world, BlockPos pos, EnumFacing facing) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null) {
			return false;
		}
		PartFluidInterface partFluidInterface = OCUtils.getPart(world, pos, AEPartLocation.INTERNAL, PartFluidInterface.class);
		return partFluidInterface != null || tile instanceof IFluidInterface;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile == null || (!(tile instanceof IPartHost || tile instanceof IFluidInterface))) {
			return null;
		}
		return new Enviroment(tile);
	}

	public class Enviroment extends AbstractManagedEnvironment implements NamedBlock {

		protected final TileEntity tile;
		protected final IPartHost host;

		public Enviroment(TileEntity tile) {
			this.tile = tile;
			this.host = tile instanceof IPartHost ? (IPartHost) tile : null;
			setNode(Network.newNode(this, Visibility.Network).
				withComponent("me_interface").
				create());
		}

		@Callback(doc = "function(side:number):table -- Get the configuration of the fluid interface on the specified direction.")
		public Object[] getFluidInterfaceConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			if (tile instanceof TileEntityFluidInterface) {
				TileEntityFluidInterface fluidInterface = (TileEntityFluidInterface) tile;
				Fluid fluid = fluidInterface.getFilter(dir);
				if (fluid == null) {
					return new Object[]{null};
				}
				return new Object[]{new FluidStack(fluid, 1000)};
			}
			PartFluidInterface part = OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartFluidInterface.class);
			if (part == null) {
				return new Object[]{null, "no interface"};
			}
			Fluid fluid = part.getFilter(dir);
			if (fluid == null) {
				return new Object[]{null};
			}
			return new Object[]{new FluidStack(fluid, 1000)};

		}

		@Callback(doc = "function(side:number[, database:address, entry:number]):boolean -- Configure the filter in fluid interface on the specified direction.")
		public Object[] setFluidInterfaceConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			IFluidInterface part = tile instanceof IFluidInterface ? (IFluidInterface) tile : OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartFluidInterface.class);
			if (part == null) {
				return new Object[]{null, "no export bus"};
			}
			String address;
			int entry;
			if (args.count() == 3) {
				address = args.checkString(1);
				entry = args.checkInteger(2);
			} else {
				part.setFilter(dir, null);
				context.pause(0.5);
				return new Object[]{true};
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
					part.setFilter(dir, null);
				} else {
					FluidStack fluid = FluidHelper.getFluidFromContainer(data);
					if (fluid == null || fluid.getFluid() == null) {
						return new Object[]{false, "not a fluid container"};
					}
					part.setFilter(dir, fluid.getFluid());
				}
				context.pause(0.5);
				return new Object[]{true};
			} catch (Throwable e) {
				return new Object[]{false, "invalid slot"};
			}
		}

		@Override
		public String preferredName() {
			return "me_interface";
		}

		@Override
		public int priority() {
			return 0;
		}

	}

	@Override
	public Class<?> getEnvironment(ItemStack stack) {
		if (stack == null) {
			return null;
		}
		if (stack.getItem() == ItemEnum.PARTITEM.getItem() && stack.getItemDamage() == PartEnum.INTERFACE.ordinal()) {
			return Enviroment.class;
		}
		if (stack.getItem() == Item.getItemFromBlock(BlockEnum.ECBASEBLOCK.getBlock()) && stack.getItemDamage() == 0) {
			return Enviroment.class;
		}
		return null;
	}
}
