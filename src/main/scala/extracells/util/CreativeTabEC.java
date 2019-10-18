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
	public ItemStack createIcon() {
		return ItemEnum.FLUIDSTORAGE.getSizedStack(1);
	}
}
