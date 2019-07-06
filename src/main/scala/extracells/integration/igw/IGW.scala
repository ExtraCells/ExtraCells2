package extracells.integration.igw

import java.util

import cpw.mods.fml.common.Optional
import cpw.mods.fml.common.registry.GameRegistry
import extracells.Extracells
import extracells.integration.Integration
import extracells.registries.{BlockEnum, ItemEnum}
import igwmod.api.WikiRegistry
import net.minecraft.item.{Item, ItemStack}

import scala.collection.JavaConversions._

object IGW {

  def initNotifier {
    IGWSupportNotifier
  }

  @Optional.Method(modid = "IGWMod")
  def init{
    for(item <- ItemEnum.values()){
      if(item != ItemEnum.CRAFTINGPATTERN && item != ItemEnum.FLUIDITEM) {
        if(item == ItemEnum.FLUIDPATTERN){
          WikiRegistry.registerBlockAndItemPageEntry(item.getSizedStack(1), item.getSizedStack(1).getUnlocalizedName.replace(".", "/"))
        } else if (item == ItemEnum.STORAGECOMPONENT || item == ItemEnum.STORAGECASING){
          val list = new util.ArrayList[java.lang.Object]
          item.getItem.getSubItems(item.getItem, Extracells.ModTab, list)
          for (sub <- list) {
            val stack = sub.asInstanceOf[ItemStack]
            WikiRegistry.registerBlockAndItemPageEntry(stack, "extracells/item/crafting")
          }
        } else{
          val list = new util.ArrayList[java.lang.Object]
          item.getItem.getSubItems(item.getItem, Extracells.ModTab, list)
          for (sub <- list) {
            val stack = sub.asInstanceOf[ItemStack]
            WikiRegistry.registerBlockAndItemPageEntry(stack, stack.getUnlocalizedName.replace(".", "/"))
          }
        }
      }
    }

    if(Integration.Mods.OPENCOMPUTERS.isEnabled){
      val stack = GameRegistry.findItemStack("extracells", "oc.upgrade", 1)
      WikiRegistry.registerBlockAndItemPageEntry(stack.getItem, stack.getUnlocalizedName.replace(".", "/"))
    }

    for(block <- BlockEnum.values()){

      val list = new util.ArrayList[java.lang.Object]
      Item.getItemFromBlock(block.getBlock).getSubItems(Item.getItemFromBlock(block.getBlock), Extracells.ModTab, list)
      for(sub <- list){
        val stack = sub.asInstanceOf[ItemStack]
        WikiRegistry.registerBlockAndItemPageEntry(stack, stack.getUnlocalizedName.replace(".", "/").replace("tile/", ""))
      }
    }
  }
}
