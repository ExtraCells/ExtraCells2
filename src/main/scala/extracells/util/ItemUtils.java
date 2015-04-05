package extracells.util;

import net.minecraft.item.ItemStack;

public class ItemUtils {
	
	public static boolean areItemEqualsIgnoreStackSize(ItemStack stack1, ItemStack stack2){
		if(stack1 == null && stack2 == null)
			return true;
		else if(stack1 == null || stack2 == null)
			return false;
		ItemStack s1 = stack1.copy();
		ItemStack s2 = stack2.copy();
		s1.stackSize = 1;
		s2.stackSize = 1;
		return ItemStack.areItemStacksEqual(s1, s2);
	}

}
