package extracells.registries;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import extracells.Extracells;
import extracells.item.*;

public enum ItemEnum
{
	PARTITEM("part.base", new ItemPartECBase()),
	FLUIDSTORAGE("storage.fluid", new ItemStorageFluid()),
	PHYSICALSTORAGE("storage.physical", new ItemStoragePhysical()),
	FLUIDPATTERN("pattern.fluid", new ItemFluidPattern()),
	FLUIDWIRELESSTERMINAL("terminal.fluid.wireless", new ItemWirelessTerminalFluid()),
	STORAGECOMPONET("storage.component", new ItemStorageComponent()),
	STORAGECASING("storage.casing", new ItemStorageCasing());

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

	public ItemStack getDamagedStack(int damage)
	{
		return new ItemStack(item, 1, damage);
	}

	public ItemStack getSizedStack(int size)
	{
		return new ItemStack(item, size);
	}
}
