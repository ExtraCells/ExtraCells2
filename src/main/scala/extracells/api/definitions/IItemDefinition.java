package extracells.api.definitions;


public interface IItemDefinition {

	// Fluid Storage Components
	appeng.api.definitions.IItemDefinition cell1kPartFluid();

	appeng.api.definitions.IItemDefinition cell4kPartFluid();

	appeng.api.definitions.IItemDefinition cell16kPartFluid();

	appeng.api.definitions.IItemDefinition cell64kPartFluid();

	appeng.api.definitions.IItemDefinition cell256kPartFluid();

	appeng.api.definitions.IItemDefinition cell1024kPartFluid();

	appeng.api.definitions.IItemDefinition cell4096kPartFluid();

	// Physical Storage Components
	appeng.api.definitions.IItemDefinition cell256kPart();

	appeng.api.definitions.IItemDefinition cell1024kPart();

	appeng.api.definitions.IItemDefinition cell4096kPart();

	appeng.api.definitions.IItemDefinition cell16384kPart();

	// Fluid Storage Casing
	appeng.api.definitions.IItemDefinition fluidCasing();

	// Fluid Cells
	appeng.api.definitions.IItemDefinition fluidCell1k();

	appeng.api.definitions.IItemDefinition fluidCell4k();

	appeng.api.definitions.IItemDefinition fluidCell16k();

	appeng.api.definitions.IItemDefinition fluidCell64k();

	appeng.api.definitions.IItemDefinition fluidCell256k();

	appeng.api.definitions.IItemDefinition fluidCell1024k();

	appeng.api.definitions.IItemDefinition fluidCell4096k();

	appeng.api.definitions.IItemDefinition fluidCellPortable();

	// Physical Storage Casing
	appeng.api.definitions.IItemDefinition physCasing();

	// Physical Cells
	appeng.api.definitions.IItemDefinition physCell256k();

	appeng.api.definitions.IItemDefinition physCell1024k();

	appeng.api.definitions.IItemDefinition physCell4096k();

	appeng.api.definitions.IItemDefinition physCell16384k();

	appeng.api.definitions.IItemDefinition physCellContainer();

	// MISC
	appeng.api.definitions.IItemDefinition wirelessFluidTerminal();

	appeng.api.definitions.IItemDefinition itemFluidPattern();
}
