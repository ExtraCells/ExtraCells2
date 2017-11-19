package extracells.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemStackUtils {

	public static boolean isEmpty(ItemStack stack) {
		return stack == null || stack.isEmpty();
	}

	public static ItemStack removeFromStack(@Nonnull ItemStack source, @Nonnull ItemStack toRemove){
		if(isEqual(source, toRemove)){
			ItemStack removed = source.copy();
			if(removed.getCount() > toRemove.getCount()){
				removed.setCount(removed.getCount() - toRemove.getCount());
				return removed;
			}
			return ItemStack.EMPTY;
		}
		return source;
	}

	public static boolean isEqual(ItemStack stack0, ItemStack stack1){
		return ItemStack.areItemsEqual(stack0, stack1) && ItemStack.areItemStackTagsEqual(stack0, stack1);
	}
}
