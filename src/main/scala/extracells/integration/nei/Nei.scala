package extracells.integration.nei

import java.util

import codechicken.nei.api.API
import extracells.Extracells
import extracells.registries.{BlockEnum, ItemEnum}
import net.minecraft.item.{Item, ItemStack}


object Nei {

  def hideItems = {
    API.hideItem(new ItemStack(ItemEnum.FLUIDITEM.getItem))
    API.hideItem(new ItemStack(ItemEnum.CRAFTINGPATTERN.getItem))
    for (item <- ItemEnum.values()) {
      if (item.getMod != null && (!item.getMod.isEnabled)) {
        val i = item.getItem
        val list = new util.ArrayList[ItemStack]
        i.getSubItems(i, Extracells.ModTab, list)
        val it = list.iterator
        while(it.hasNext){
          API.hideItem(it.next)
        }
      }
    }

    for (block <- BlockEnum.values()) {
      if (block.getMod != null && (!block.getMod.isEnabled)) {
        val b = block.getBlock
        val list = new util.ArrayList[ItemStack]
        b.getSubBlocks(Item.getItemFromBlock(b), Extracells.ModTab, list)
        val it = list.iterator
        while(it.hasNext){
          API.hideItem(it.next)
        }
      }
    }
  }

  def init = {
    hideItems
    if(Extracells.proxy.isClient){
      val handler = new UniversalTerminalRecipe
      API.registerUsageHandler(handler)
      API.registerRecipeHandler(handler)
    }

  }

}
