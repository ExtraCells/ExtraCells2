package extracells;

import net.minecraft.block.Block;
import net.minecraft.util.StatCollector;
import extracells.blocks.BlockBusFluidExport;
import extracells.blocks.BlockBusFluidImport;
import extracells.blocks.BlockBusFluidStorage;
import extracells.blocks.BlockCertusTank;
import extracells.blocks.BlockFluidTransitionPlane;
import extracells.blocks.BlockHardMEDrive;
import extracells.blocks.BlockInterFaceFluid;
import extracells.blocks.BlockLevelEmitterFluid;
import extracells.blocks.BlockMEBattery;
import extracells.blocks.BlockMEDropper;
import extracells.blocks.BlockSolderingStation;
import extracells.blocks.BlockTerminalFluid;
import extracells.blocks.BlockWalrus;

public enum BlockEnum
{
	SOLDERINGSTATION("tile.block.solderingstation.name", 500, BlockSolderingStation.class, "ID for the soldering station", "SolderingStation"),
	MEDROPPER("tile.block.medropper.name", 501, BlockMEDropper.class, "ID for the ME Item Dropper", "MEDropper"),
	MEBATTERY("tile.block.mebattery.name", 502, BlockMEBattery.class, "ID for the ME Backup Battery", "MEBattery"),
	BLASTRESISTANTMEDRIVE("tile.block.hardmedrive.name", 503, BlockHardMEDrive.class, "ID for the Blast Resistant ME Drive", "HardMEDrive"),
	FLUIDIMPORT("tile.block.fluid.bus.import.name", 504, BlockBusFluidImport.class, "ID for the Fluid Import Bus", "BusFluidImport"),
	FLUIDEXPORT("tile.block.fluid.bus.export.name", 505, BlockBusFluidExport.class, "ID for the Fluid Export Bus", "BusFluidExport"),
	FLUIDSTORAGE("tile.block.fluid.bus.storage.name", 506, BlockBusFluidStorage.class, "ID for the Fluid Storage Bus", "BusFluidStorage"),
	FLUIDTERMINAL("tile.block.fluid.terminal.name", 507, BlockTerminalFluid.class, "ID for the Fluid Storage Terminal", "TerminalFluid"),
	FLUIDTRANSITION("tile.block.fluid.transitionplane.name", 508, BlockFluidTransitionPlane.class, "ID for the Fluid Transition Plance", "FluidTransitionPlane"),
	CERTUSTANK("tile.block.certustank.name", 509, BlockCertusTank.class, "ID for the ME Certus Tank", "CertusTank"),
	CHROMIA("tile.block.walrus.name", 510, BlockWalrus.class, "ID for the Walrus", "Walrus"),
	FLUIDLEVELEMITTER("tile.block.fluid.levelemitter.name", 511, BlockLevelEmitterFluid.class, "ID for the ME Fluid Level Emitter", "LevelEmitterFluid"),
	FLUIDINTERFACE("tile.block.fluid.interface.name", 512, BlockInterFaceFluid.class, "ID for the ME Fluid Interface", "InterfaceFluid");

	private final String internalName;
	private String description, IDName;
	private int ID;
	private Block block;
	private Class<? extends Block> blockClass;

	BlockEnum(String internalName, int ID, Class<? extends Block> blockClass, String description, String IDName)
	{
		this.internalName = internalName;
		this.ID = ID;
		this.blockClass = blockClass;
		this.description = description;
		this.IDName = IDName;
	}

	public String getLocalizedName()
	{
		return StatCollector.translateToLocal(internalName);
	}

	public void setID(int ID)
	{
		this.ID = ID;
	}

	public int getID()
	{
		return ID;
	}

	public void setBlockEntry(Block block)
	{
		this.block = block;
	}

	public Block getBlockEntry()
	{
		return block;
	}

	public String getDescription()
	{
		return description;
	}

	public String getIDName()
	{
		return IDName;
	}

	public Class<? extends Block> getBlockClass()
	{
		return blockClass;
	}
}
