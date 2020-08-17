package extracells.integration.opencomputers;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import extracells.api.IFluidInterface;
import extracells.part.PartFluidInterface;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.FluidUtil;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.internal.Database;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;
import li.cil.oc.server.network.Component;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public class DriverFluidInterface implements SidedBlock{

	@Override
	public boolean worksWith(World world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		return tile != null && (getFluidInterface(world, x, y, z, side) != null || tile instanceof IFluidInterface);
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile == null || (!(tile instanceof IPartHost || tile instanceof IFluidInterface)))
			return null;
		return new Environment(tile);
	}
	
	private static PartFluidInterface getFluidInterface(World world, int x, int y, int z, ForgeDirection dir){
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile == null || (!(tile instanceof IPartHost)))
			return null;
		IPartHost host = (IPartHost) tile;
		if(dir == null || dir == ForgeDirection.UNKNOWN){
			for (ForgeDirection side: ForgeDirection.VALID_DIRECTIONS){
				IPart part = host.getPart(side);
				if (part != null && part instanceof PartFluidInterface)
					return (PartFluidInterface) part;
			}
			return null;
		}else{
			IPart part = host.getPart(dir);
			return part instanceof PartFluidInterface ? (PartFluidInterface) part : null;
		}
	}
	
	public class Environment extends ManagedEnvironment implements NamedBlock{
		
		protected final TileEntity tile;
		protected final IPartHost host;
		
		Environment(TileEntity tile){
			this.tile =  tile;
			this.host = tile instanceof IPartHost ? (IPartHost) tile : null;
			setNode(Network.newNode(this, Visibility.Network).
	                withComponent("me_interface").
	                create());
		}

		@Callback(doc = "function(side:number):table -- Get the configuration of the fluid interface on the specified direction.")
		public Object[] getFluidInterfaceConfiguration(Context context, Arguments args){
			ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
			if (dir == null || dir == ForgeDirection.UNKNOWN)
				return new Object[]{null, "unknown side"};
			if (tile instanceof TileEntityFluidInterface){
				TileEntityFluidInterface fluidInterface = (TileEntityFluidInterface) tile;
				Fluid fluid = fluidInterface.getFilter(dir);
				if (fluid == null)
					return new Object[]{null};
				return new Object[]{new FluidStack(fluid, 1000)};
			}
			PartFluidInterface part = getFluidInterface(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
			if (part == null)
				return new Object[]{null, "no interface"};
			Fluid fluid = part.getFilter(dir);
			if (fluid == null)
				return new Object[]{null};
			return new Object[]{new FluidStack(fluid, 1000)};
			
		}
		
		@Callback(doc = "function(side:number[, database:address, entry:number]):boolean -- Configure the filter in fluid interface on the specified direction.")
		public Object[] setFluidInterfaceConfiguration(Context context, Arguments args){
			ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
			if (dir == null || dir == ForgeDirection.UNKNOWN)
				return new Object[]{null, "unknown side"};
			IFluidInterface part = tile instanceof IFluidInterface ? (IFluidInterface) tile : getFluidInterface(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
			if (part == null)
				return new Object[]{null, "no export bus"};
			String address;
			int entry;
			if (args.count() == 3){
				address = args.checkString(1);
				entry = args.checkInteger(2);
			}else{
				part.setFilter(dir, null);
				context.pause(0.5);
				return new Object[]{true};
			}
			Node node = node().network().node(address);
			if (node == null)
				throw new IllegalArgumentException("no such component");
			if (!(node instanceof Component))
				throw new IllegalArgumentException("no such component");
			Component component = (Component) node;
			li.cil.oc.api.network.Environment env = node.host();
			if (!(env instanceof Database))
				throw new IllegalArgumentException("not a database");
			Database database = (Database) env;
			try{
				ItemStack data = database.getStackInSlot(entry - 1);
				if (data == null)
					part.setFilter(dir, null);
				else{
					FluidStack fluid = FluidUtil.getFluidFromContainer(data);
					if(fluid == null || fluid.getFluid() == null)
						return new Object[]{false, "not a fluid container"};
					part.setFilter(dir, fluid.getFluid());
				}
				context.pause(0.5);
				return new Object[]{true};
			}catch(Throwable e){
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

	static class Provider implements EnvironmentProvider {
		@Override
		public Class<? extends li.cil.oc.api.network.Environment> getEnvironment(ItemStack stack) {
			if (stack == null)
				return null;
			if (stack.getItem() == ItemEnum.PARTITEM.getItem() && stack.getItemDamage() == PartEnum.INTERFACE.ordinal())
				return Environment.class;
			if (stack.getItem() == Item.getItemFromBlock(BlockEnum.ECBASEBLOCK.getBlock()) && stack.getItemDamage() == 0)
				return Environment.class;
			return null;
		}
	}
}
