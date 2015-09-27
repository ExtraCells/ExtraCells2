package extracells.definitions;

import appeng.api.definitions.IItemDefinition;
import extracells.api.definitions.IPartDefinition;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;

public class PartDefinition implements IPartDefinition {

	public static final PartDefinition instance = new PartDefinition();

	@Override
	public IItemDefinition partBattery() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.BATTERY.ordinal());
	}

	@Override
	public IItemDefinition partConversionMonitor() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDCONVERSIONMONITOR.ordinal());
	}

	@Override
	public IItemDefinition partDrive() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.DRIVE.ordinal());
	}

	@Override
	public IItemDefinition partFluidAnnihilationPlane() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDPANEANNIHILATION.ordinal());
	}

	@Override
	public IItemDefinition partFluidExportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDIMPORT.ordinal());
	}

	@Override
	public IItemDefinition partFluidFormationPlane() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDPANEFORMATION.ordinal());
	}

	@Override
	public IItemDefinition partFluidImportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDEXPORT.ordinal());
	}

	@Override
	public IItemDefinition partFluidLevelEmitter() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDLEVELEMITTER.ordinal());
	}

	@Override
	public IItemDefinition partFluidStorageBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDSTORAGE.ordinal());
	}

	@Override
	public IItemDefinition partFluidTerminal() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDTERMINAL.ordinal());
	}

	@Override
	public IItemDefinition partInterface() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.INTERFACE.ordinal());
	}

	@Override
	public IItemDefinition partOreDictExportBus() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.OREDICTEXPORTBUS.ordinal());
	}

	@Override
	public IItemDefinition partStorageMonitor() {
		return new ItemItemDefinitions(ItemEnum.PARTITEM.getItem(),
				PartEnum.FLUIDMONITOR.ordinal());
	}

}
