package extracells.integration.igw

import extracells.integration.Integration
import extracells.registries.{BlockEnum, ItemEnum}
import extracells.util.CreativeTabEC
import igwmod.api.WikiRegistry
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.common.Optional

import scala.collection.JavaConversions._

object IGW {

  def initNotifier {
    IGWSupportNotifier
  }

  @Optional.Method(modid = "igwmod")
  def init{
    for(item <- ItemEnum.values()){
      if(item != ItemEnum.CRAFTINGPATTERN && item != ItemEnum.FLUIDITEM && item != ItemEnum.GASITEM && item != ItemEnum.OCUPGRADE) {
        if(item == ItemEnum.FLUIDPATTERN){
          WikiRegistry.registerBlockAndItemPageEntry(item.getSizedStack(1), item.getSizedStack(1).getTranslationKey.replace(".", "/"))
        }else if (item == ItemEnum.STORAGECOMPONET || item == ItemEnum.STORAGECASING){
          val list = NonNullList.create[ItemStack]
          item.getItem.getSubItems(CreativeTabEC.INSTANCE, list)
          for (sub <- list) {
            val stack = sub.asInstanceOf[ItemStack]
            WikiRegistry.registerBlockAndItemPageEntry(stack, "extracells/item/crafting")
          }
        }else{
          val list = NonNullList.create[ItemStack]
          item.getItem.getSubItems(CreativeTabEC.INSTANCE, list)
          for (sub <- list) {
            val stack = sub.asInstanceOf[ItemStack]
            WikiRegistry.registerBlockAndItemPageEntry(stack, stack.getTranslationKey.replace(".", "/"))
          }
        }
      }
    }

    if(Integration.Mods.OPENCOMPUTERS.isEnabled){
      val stack = ItemEnum.OCUPGRADE.getSizedStack(1)
      WikiRegistry.registerBlockAndItemPageEntry(stack.getItem, stack.getTranslationKey.replace(".", "/"))
    }

    for(block <- BlockEnum.values()){

      val list = NonNullList.create[ItemStack]
      Item.getItemFromBlock(block.getBlock).getSubItems( CreativeTabEC.INSTANCE, list)
      for(sub <- list){
        val stack = sub.asInstanceOf[ItemStack]
        WikiRegistry.registerBlockAndItemPageEntry(stack, stack.getTranslationKey.replace(".", "/").replace("tile/", ""))
      }
    }
  }
}
