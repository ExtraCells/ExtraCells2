package extracells.util

import net.minecraft.item.ItemStack

object ExtendedItemStack {

  implicit def extendedItemStack(stack: ItemStack): ExtendedItemStack = new ExtendedItemStack(stack)

  class ExtendedItemStack(val stack: ItemStack){

    def setCount(count: Int) {
      stack.stackSize=count
    }

    def getCount(): Int = stack.stackSize

  }
}
