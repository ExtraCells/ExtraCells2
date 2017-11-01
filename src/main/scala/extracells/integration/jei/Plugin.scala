package extracells.integration.jei

import extracells.registries.{BlockEnum, ItemEnum}
import mezz.jei.api._
import mezz.jei.api.ingredients.IModIngredientRegistration
import net.minecraft.item.{Item, ItemStack}
import java.util

import extracells.integration.Integration
import extracells.util.CreativeTabEC
import net.minecraft.util.NonNullList

@JEIPlugin
class Plugin extends BlankModPlugin{
  override def registerItemSubtypes(subtypeRegistry: ISubtypeRegistry) {
  }

  override def registerIngredients(ingredientRegistry: IModIngredientRegistration) {
  }

  override def register(registry: IModRegistry): Unit = {
    if (!Integration.Mods.JEI.isEnabled)
      return

    Jei.registry = registry


    for (item <- Jei.fluidBlacklist){
      registry.getJeiHelpers.getIngredientBlacklist.addIngredientToBlacklist(item)
    }


    hideItem(new ItemStack(ItemEnum.FLUIDITEM.getItem), registry)
    hideItem(new ItemStack(ItemEnum.CRAFTINGPATTERN.getItem), registry)
    for (item <- ItemEnum.values()) {
      if (item.getMod != null && (!item.getMod.isEnabled)) {
        val i = item.getItem
        val list = NonNullList.create[ItemStack]()
        i.getSubItems(CreativeTabEC.INSTANCE, list)
        val it = list.iterator
        while(it.hasNext){
          hideItem(it.next, registry)
        }
      }
    }

    for (block <- BlockEnum.values()) {
      if (block.getMod != null && (!block.getMod.isEnabled)) {
        val b = block.getBlock
        val list = NonNullList.create[ItemStack]()
        b.getSubBlocks(CreativeTabEC.INSTANCE, list)
        val it = list.iterator
        while(it.hasNext){
          hideItem(it.next, registry)
        }
      }
    }
  }

  override def onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
  }

  private def hideItem(item: ItemStack, registry: IModRegistry): Unit ={
    registry.getJeiHelpers.getIngredientBlacklist.addIngredientToBlacklist(item)
  }


}
