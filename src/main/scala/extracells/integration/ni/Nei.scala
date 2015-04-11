package extracells.integration.ni

import codechicken.nei.api.API
import extracells.registries.ItemEnum
import net.minecraft.item.ItemStack


object Nei {

  def hideItems = {
    API.hideItem(new ItemStack(ItemEnum.FLUIDITEM.getItem))
    API.hideItem(new ItemStack(ItemEnum.CRAFTINGPATTERN.getItem))
  }

  def init = {
    hideItems
  }

}
