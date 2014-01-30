package extracells;

import extracells.item.ItemECBasePart;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;

public enum ItemEnum
{
	PARTITEM("item.part.base", 4140, ItemECBasePart.class, "The item used for all the bus parts", "ItemECBasePart");

	private final String internalName;
	private String description, IDName;
	private int ID;
	private Item item;
	private Class<? extends Item> itemClass;

	ItemEnum(String internalName, int ID, Class<? extends Item> itemClass, String description, String IDName)
	{
		this.internalName = internalName;
		this.ID = ID;
		this.itemClass = itemClass;
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

	public void setItemInstance(Item item)
	{
		this.item = item;
	}

	public Item getItemInstance()
	{
		return item;
	}

	public String getDescription()
	{
		return description;
	}

	public String getIDName()
	{
		return IDName;
	}

	public Class<? extends Item> getItemClass()
	{
		return itemClass;
	}
}
