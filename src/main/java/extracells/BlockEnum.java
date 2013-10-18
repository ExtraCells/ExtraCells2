package extracells;

import net.minecraft.block.Block;
import net.minecraft.util.StatCollector;

public enum BlockEnum
{
	CERTUSTANK("tile.block.certustank.name"),
	FLUIDIMPORT("tile.block.fluid.bus.import.name"),
	FLUIDEXPORT("tile.block.fluid.bus.export.name"),
	FLUIDSTORAGE("tile.block.fluid.bus.storage.name"),
	FLUIDTERMINAL("tile.block.fluid.terminal.name"),
	FLUIDTRANSITION("tile.block.fluid.transitionplane.name"),
	BRMEDRIVE("tile.block.hardmedrive.name"),
	MEDROPPER("tile.block.medropper.name"),
	MEBATTERY("tile.block.mebattery.name"),
	SOLDERINGSTATION("tile.block.solderingstation.name"),
	CHROMIA("tile.block.walrus.name");

	private final String internalName;
	private int ID;
	private Block block;

	BlockEnum(String internalName)
	{
		this.internalName = internalName;
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
}
