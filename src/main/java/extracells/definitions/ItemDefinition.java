package extracells.definitions;

import appeng.api.util.AEItemDefinition;
import extracells.api.definitions.IItemDefinition;
import extracells.registries.ItemEnum;

public class ItemDefinition implements IItemDefinition {

	public static final ItemDefinition instance = new ItemDefinition();
	
	//Fluid Cells
	@Override
	public AEItemDefinition fluidCell1k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem());
	}

	@Override
	public AEItemDefinition fluidCell4k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 1);
	}

	@Override
	public AEItemDefinition fluidCell16k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 2);
	}

	@Override
	public AEItemDefinition fluidCell64k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 3);
	}

	@Override
	public AEItemDefinition fluidCell256k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 4);
	}

	@Override
	public AEItemDefinition fluidCell1024k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 5);
	}

	@Override
	public AEItemDefinition fluidCell4096k() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGE.getItem(), 6);
	}
	
	@Override
	public AEItemDefinition fluidCellPortable() {
		return new ItemItemDefinitions(ItemEnum.FLUIDSTORAGEPORTABLE.getItem());
	}

	
	
	//Physical Cells
	@Override
	public AEItemDefinition physCell256k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem());
	}

	@Override
	public AEItemDefinition physCell1024k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 1);
	}

	@Override
	public AEItemDefinition physCell4096k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 2);
	}

	@Override
	public AEItemDefinition physCell16384k() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 3);
	}
	
	@Override
	public AEItemDefinition physCellContainer() {
		return new ItemItemDefinitions(ItemEnum.PHYSICALSTORAGE.getItem(), 4);
	}

	
	
	//Fluid Storage Components
	@Override
	public AEItemDefinition cell1kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 4);
	}

	@Override
	public AEItemDefinition cell4kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 5);
	}

	@Override
	public AEItemDefinition cell16kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 6);
	}

	@Override
	public AEItemDefinition cell64kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 7);
	}

	@Override
	public AEItemDefinition cell256kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 8);
	}

	@Override
	public AEItemDefinition cell1024kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 9);
	}

	@Override
	public AEItemDefinition cell4096kPartFluid() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 10);
	}

	
	
	//Physical Storage Components
	@Override
	public AEItemDefinition cell256kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem());
	}

	@Override
	public AEItemDefinition cell1024kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 1);
	}

	@Override
	public AEItemDefinition cell4096kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 2);
	}

	@Override
	public AEItemDefinition cell16384kPart() {
		return new ItemItemDefinitions(ItemEnum.STORAGECOMPONET.getItem(), 3);
	}

	
	
	//Physical Storage Casing
	@Override
	public AEItemDefinition physCasing() {
		return new ItemItemDefinitions(ItemEnum.STORAGECASING.getItem());
	}

	
	
	//Fluid Storage
	@Override
	public AEItemDefinition fluidCasing() {
		return new ItemItemDefinitions(ItemEnum.STORAGECASING.getItem(), 1);
	}

	@Override
	public AEItemDefinition wirelessFluidTerminal() {
		return new ItemItemDefinitions(ItemEnum.FLUIDWIRELESSTERMINAL.getItem());
	}
}
