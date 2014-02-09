package extracells;

import extracells.item.ItemPartECBase;
import extracells.item.ItemStorageFluid;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;

public enum ItemEnum
{
	PARTITEM("item.part.base", new ItemPartECBase()),
	FLUIDSTORAGE("item.storage.fluid", new ItemStorageFluid());

	private final String internalName;
	private Item item;

	ItemEnum(String _internalName, Item _item)
	{
		internalName = "extracells." + _internalName;
		item = _item;
		item.setUnlocalizedName(internalName);
		item.setCreativeTab(Extracells.ModTab);
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal(internalName);
	}

	public String getInternalName()
	{
		return internalName;
	}

	public Item getItem()
	{
		return item;
	}
}
