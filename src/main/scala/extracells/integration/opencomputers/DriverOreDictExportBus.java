package extracells.integration.opencomputers;

import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.AbstractManagedEnvironment;
import net.minecraft.tileentity.TileEntity;

import appeng.api.parts.IPartHost;
import appeng.api.util.AEPartLocation;
import extracells.part.PartOreDictExporter;
import extracells.registries.PartEnum;
import li.cil.oc.api.Network;
import li.cil.oc.api.driver.NamedBlock;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.Visibility;

public class DriverOreDictExportBus extends DriverBase<PartOreDictExporter> {

	public DriverOreDictExportBus() {
		super(PartEnum.OREDICTEXPORTBUS, Enviroment.class);
	}

	@Override
	protected ManagedEnvironment createEnvironment(IPartHost host) {
		return new Enviroment(host);
	}

	public class Enviroment extends AbstractManagedEnvironment implements NamedBlock {

		protected final TileEntity tile;
		protected final IPartHost host;

		public Enviroment(IPartHost host) {
			tile = (TileEntity) host;
			this.host = host;
			setNode(Network.newNode(this, Visibility.Network).
				withComponent("me_exportbus").
				create());
		}

		@Callback(doc = "function(side:number):string -- Get the configuration of the ore dict export bus pointing in the specified direction.")
		public Object[] getOreConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			PartOreDictExporter part = OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartOreDictExporter.class);
			if (part == null) {
				return new Object[]{null, "no export bus"};
			}
			return new Object[]{part.getFilter()};
		}

		@Callback(doc = "function(side:number[, filter:string]):boolean -- Set the configuration of the ore dict export bus pointing in the specified direction.")
		public Object[] setOreConfiguration(Context context, Arguments args) {
			AEPartLocation dir = AEPartLocation.fromOrdinal(args.checkInteger(0));
			if (dir == null || dir == AEPartLocation.INTERNAL) {
				return new Object[]{null, "unknown side"};
			}
			PartOreDictExporter part = OCUtils.getPart(tile.getWorld(), tile.getPos(), dir, PartOreDictExporter.class);
			if (part == null) {
				return new Object[]{false, "no export bus"};
			}
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

}
