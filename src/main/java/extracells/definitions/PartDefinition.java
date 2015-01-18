package extracells.definitions;

import appeng.api.util.AEItemDefinition;
import extracells.api.definitions.IPartDefinition;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;

public class PartDefinition implements IPartDefinition {
	
	public static final PartDefinition instance = new PartDefinition();

	@Override
	public AEItemDefinition partFluidImportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDEXPORT.ordinal());
	}

	@Override
	public AEItemDefinition partFluidExportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDIMPORT.ordinal());
	}

	@Override
	public AEItemDefinition partFluidStorageBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDSTORAGE.ordinal());
	}

	@Override
	public AEItemDefinition partFluidTerminal() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDTERMINAL.ordinal());
	}

	@Override
	public AEItemDefinition partFluidLevelEmitter() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDLEVELEMITTER.ordinal());
	}

	@Override
	public AEItemDefinition partFluidAnnihilationPlane() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDPANEANNIHILATION.ordinal());
	}

	@Override
	public AEItemDefinition partFluidFormationPlane() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDPANEFORMATION.ordinal());
	}

	@Override
	public AEItemDefinition partBattery() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.BATTERY.ordinal());
	}

	@Override
	public AEItemDefinition partDrive() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.DRIVE.ordinal());
	}

	@Override
	public AEItemDefinition partInterface() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.INTERFACE.ordinal());
	}

	@Override
	public AEItemDefinition partStorageMonitor() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDMONITOR.ordinal());
	}

	@Override
	public AEItemDefinition partConversionMonitor() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.FLUIDCONVERSIONMONITOR.ordinal());
	}

	@Override
	public AEItemDefinition partOreDictExportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(), PartEnum.OREDICTEXPORTBUS.ordinal());
	}

}
