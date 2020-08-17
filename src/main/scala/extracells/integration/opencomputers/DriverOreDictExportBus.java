package extracells.integration.opencomputers;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import extracells.part.PartOreDictExporter;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.EnvironmentProvider;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.driver.SidedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;
import li.cil.oc.api.prefab.ManagedEnvironment;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public class DriverOreDictExportBus implements SidedBlock{

	@Override
	public boolean worksWith(World world, int x, int y, int z, ForgeDirection side) {
		return getExportBus(world, x, y, z, side) != null;
	}

	@Override
	public ManagedEnvironment createEnvironment(World world, int x, int y, int z, ForgeDirection side) {
		TileEntity tile = world.getTileEntity(x, y, z);
		if (!(tile instanceof IPartHost))
			return null;
		return new Environment((IPartHost) tile);
	}
	
	private static PartOreDictExporter getExportBus(World world, int x, int y, int z, ForgeDirection dir){
		TileEntity tile = world.getTileEntity(x, y, z);
		if ((!(tile instanceof IPartHost)))
			return null;
		IPartHost host = (IPartHost) tile;
		if(dir == null || dir == ForgeDirection.UNKNOWN){
			for (ForgeDirection side: ForgeDirection.VALID_DIRECTIONS){
				IPart part = host.getPart(side);
				if (part instanceof PartOreDictExporter)
					return (PartOreDictExporter) part;
			}
			return null;
		}else{
			IPart part = host.getPart(dir);
			return part instanceof PartOreDictExporter ? (PartOreDictExporter) part : null;
		}
	}
	
	public static class Environment extends ManagedEnvironment implements NamedBlock{
		
		protected final TileEntity tile;
		protected final IPartHost host;
		
		Environment(IPartHost host){
			tile = (TileEntity) host;
			this.host = host;
			setNode(Network.newNode(this, Visibility.Network).
	                withComponent("me_exportbus").
	                create());
		}

		@Callback(doc = "function(side:number):string -- Get the configuration of the ore dict export bus pointing in the specified direction.")
		public Object[] getOreConfiguration(Context context, Arguments args){
			ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
			if (dir == null || dir == ForgeDirection.UNKNOWN)
				return new Object[]{null, "unknown side"};
			PartOreDictExporter part = getExportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
			if (part == null)
				return new Object[]{null, "no export bus"};
			return new Object[]{part.getFilter()};
		}
		
		@Callback(doc = "function(side:number[, filter:string]):boolean -- Set the configuration of the ore dict export bus pointing in the specified direction.")
		public Object[] setOreConfiguration(Context context, Arguments args){
			ForgeDirection dir = ForgeDirection.getOrientation(args.checkInteger(0));
			if (dir == null || dir == ForgeDirection.UNKNOWN)
				return new Object[]{false, "unknown side"};
			PartOreDictExporter part = getExportBus(tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord, dir);
			if (part == null)
				return new Object[]{false, "no export bus"};
			part.setFilter(args.optString(1, ""));
			context.pause(0.5);
			return new Object[]{true};
		}
		

		@Override
		public String preferredName() {
			return "me_exportbus";
		}

		@Override
		public int priority() {
			return 0;
		}	
		
	}

	static class Provider implements EnvironmentProvider {
		@Override
		public Class<? extends li.cil.oc.api.network.Environment> getEnvironment(ItemStack stack){
			if (stack.getItem() == ItemEnum.PARTITEM.getItem() && stack.getItemDamage() == PartEnum.OREDICTEXPORTBUS.ordinal())
				return Environment.class;
			return null;
		}
	}

}
