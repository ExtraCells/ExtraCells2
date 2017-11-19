package extracells.util

import net.minecraft.item.ItemStack
import net.minecraftforge.items.IItemHandler


object ItemHandlerUtil {

  def insertItemStack(itemHandler: IItemHandler, stack: ItemStack, simulate: Boolean): ItemStack = {
    if (itemHandler == null)
      return stack
    var itemStackRemaining = stack.copy()
    for (i <- 0 to itemHandler.getSlots - 1){
      itemStackRemaining = itemHandler.insertItem(i, itemStackRemaining, simulate)
      if (itemStackRemaining == null)
        return null;
    }
    itemStackRemaining
  }

}
