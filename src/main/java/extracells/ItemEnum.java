package extracells;

import extracells.item.ItemPartECBase;
import extracells.item.ItemStorageFluid;
import extracells.item.ItemStoragePhysical;
import net.minecraft.item.Item;
import net.minecraft.util.StatCollector;

public enum ItemEnum
{
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	FLUIDPHYSICAL("storage.physical", new ItemStoragePhysical());

	private final String internalName;
	private Item item;

	ItemEnum(String _internalName, Item _item)
	{
		internalName = _internalName;
		item = _item;
		item.setUnlocalizedName("extracells." + internalName);
		item.setCreativeTab(Extracells.ModTab);
	}

	public String getStatName()
	{
		return StatCollector.translateToLocal(item.getUnlocalizedName());
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
