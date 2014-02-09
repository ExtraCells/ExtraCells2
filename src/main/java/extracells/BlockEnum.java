package extracells;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.StatCollector;
import extracells.item.ItemBlockCertusTank;
public enum BlockEnum
{
	CERTUSTANK("tile.block.certustank.name", 509, extracells.block.BlockCertusTank.class, ItemBlockCertusTank.class, "ID for the ME Certus Tank", "CertusTank");
	private final String internalName;
	private String description, IDName;
	private int ID;
	private Block block;
	private Class<? extends Block> blockClass;
	private Class<? extends ItemBlock> itemBlockClass;

	BlockEnum(String internalName, int ID, Class<? extends Block> blockClass, String description, String IDName)
	{
		this(internalName, ID, blockClass, ItemBlock.class, description, IDName);
	}

	BlockEnum(String internalName, int ID, Class<? extends Block> blockClass, Class<? extends ItemBlock> itemBlockClass, String description, String IDName)
	{
		this.internalName = internalName;
		this.ID = ID;
		this.blockClass = blockClass;
		this.itemBlockClass = itemBlockClass;
		this.description = description;
		this.IDName = IDName;
	}

	public String getStatName()
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

	public void setBlockInstance(Block block)
	{
		this.block = block;
	}

	public Block getBlockInstance()
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

	public Class<? extends ItemBlock> getItemBlockClass()
	{
		return itemBlockClass;
	}
}
