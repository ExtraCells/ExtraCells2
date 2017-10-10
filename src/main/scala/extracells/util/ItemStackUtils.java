package extracells.util;

import net.minecraft.item.ItemStack;

public class ItemStackUtils {

	public static boolean isEmpty(ItemStack stack) {
		return stack == null;
	}

	public static ItemStack getEmpty() {
		return null;
	}
}
