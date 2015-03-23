package extracells.api.definitions;

import appeng.api.util.AEItemDefinition;

public interface IItemDefinition {

	AEItemDefinition cell1024kPart();

	AEItemDefinition cell1024kPartFluid();

	AEItemDefinition cell16384kPart();

	AEItemDefinition cell16kPartFluid();

	// Fluid Storage Components
	AEItemDefinition cell1kPartFluid();

	// Physical Storage Components
	AEItemDefinition cell256kPart();

	AEItemDefinition cell256kPartFluid();

	AEItemDefinition cell4096kPart();

	AEItemDefinition cell4096kPartFluid();

	AEItemDefinition cell4kPartFluid();

	AEItemDefinition cell64kPartFluid();

	// Fluid Storage Casing
	AEItemDefinition fluidCasing();

	AEItemDefinition fluidCell1024k();

	AEItemDefinition fluidCell16k();

	// Fluid Cells
	AEItemDefinition fluidCell1k();

	AEItemDefinition fluidCell256k();

	AEItemDefinition fluidCell4096k();

	AEItemDefinition fluidCell4k();

	AEItemDefinition fluidCell64k();

	AEItemDefinition fluidCellPortable();

	// Physical Storage Casing
	AEItemDefinition physCasing();

	AEItemDefinition physCell1024k();

	AEItemDefinition physCell16384k();

	// Physical Cells
	AEItemDefinition physCell256k();

	AEItemDefinition physCell4096k();

	AEItemDefinition physCellContainer();

	// MISC
	AEItemDefinition wirelessFluidTerminal();
}
