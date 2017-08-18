package extracells.util;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import extracells.registries.ItemEnum;

public class CreativeTabEC extends CreativeTabs {

	public static final CreativeTabs INSTANCE = new CreativeTabEC();

	public CreativeTabEC() {
		super("Extra_Cells");
	}

	@Override
	public Item getTabIconItem() {
		return ItemEnum.FLUIDSTORAGE.getItem();
	}

	@Override
	public ItemStack getIconItemStack() {
		return new ItemStack(ItemEnum.FLUIDSTORAGE.getItem());
	}
}
