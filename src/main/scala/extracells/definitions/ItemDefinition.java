package extracells.definitions;

import extracells.api.definitions.IItemDefinition;
import extracells.registries.ItemEnum;

public class ItemDefinition implements IItemDefinition {

	public static final ItemDefinition instance = new ItemDefinition();

	@Override
	public appeng.api.definitions.IItemDefinition cell1024kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 1);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell1024kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 9);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell16384kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 3);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell16kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 6);
	}

	// Fluid Storage Components
	@Override
	public appeng.api.definitions.IItemDefinition cell1kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 4);
	}

	// Physical Storage Components
	@Override
	public appeng.api.definitions.IItemDefinition cell256kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem());
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell256kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 8);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell4096kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 2);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell4096kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 10);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell4kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 5);
	}

	@Override
	public appeng.api.definitions.IItemDefinition cell64kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONENT.getItem(), 7);
	}

	// Fluid Storage
	@Override
	public appeng.api.definitions.IItemDefinition fluidCasing() {
		return new ItemItemDefinitions(ItemEnum.STORAGECASING.getItem(), 1);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell1024k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 5);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell16k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 2);
	}

	// Fluid Cells
	@Override
	public appeng.api.definitions.IItemDefinition fluidCell1k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem());
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell256k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 4);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell4096k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 6);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell4k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 1);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCell64k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 3);
	}

	@Override
	public appeng.api.definitions.IItemDefinition fluidCellPortable() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGEPORTABLE.getItem());
	}

	// Physical Storage Casing
	@Override
	public appeng.api.definitions.IItemDefinition physCasing() {
		return new ItemItemDefinitions(ItemEnum.STORAGECASING.getItem());
	}

	@Override
	public appeng.api.definitions.IItemDefinition physCell1024k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 1);
	}

	@Override
	public appeng.api.definitions.IItemDefinition physCell16384k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 3);
	}

	// Physical Cells
	@Override
	public appeng.api.definitions.IItemDefinition physCell256k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem());
	}

	@Override
	public appeng.api.definitions.IItemDefinition physCell4096k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 2);
	}

	@Override
	public appeng.api.definitions.IItemDefinition physCellContainer() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 4);
	}

	@Override
	public appeng.api.definitions.IItemDefinition wirelessFluidTerminal() {
		return new ItemItemDefinitions(ItemEnum.FLUIDWIRELESSTERMINAL.getItem());
	}

	@Override
	public appeng.api.definitions.IItemDefinition itemFluidPattern() {
		return new ItemItemDefinitions(ItemEnum.FLUIDPATTERN.getItem());
	}
}
