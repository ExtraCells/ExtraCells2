package extracells.item

import extracells.Constants
import extracells.integration.Integration
import extracells.models.{IItemModelRegister, ModelManager}
import mekanism.api.gas.GasRegistry
import net.minecraft.client.renderer.ItemMeshDefinition
import net.minecraft.client.renderer.block.model.ModelResourceLocation
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagString
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ItemGas extends Item with IItemModelRegister{

  val INSTANCE = this

  val isMekanismGasEnabled = Integration.Mods.MEKANISMGAS.isEnabled

  def setGasName(itemStack: ItemStack, fluidName: String) {
    itemStack.setTagInfo("gas", new NBTTagString(fluidName))
  }

  def getGasName(itemStack: ItemStack): String = {
    if (!itemStack.hasTagCompound) return ""
    val tagCompound = itemStack.getTagCompound
    tagCompound.getString("gas")
  }

  @SideOnly(Side.CLIENT)
  override def registerModel(item: Item, manager: ModelManager) {
    manager.registerItemModel(item, new ItemMeshDefinition {
      override def getModelLocation(i: ItemStack) = {
        if (isMekanismGasEnabled)
          new ModelResourceLocation(Constants.MOD_ID + ":gas/" + getGasName(i), "inventory")
        else
          new ModelResourceLocation(Constants.MOD_ID + ":fluid/water", "inventory")
      }
    })
  }

  @SideOnly(Side.CLIENT)
  override def getSubItems(tab: CreativeTabs, subItems: NonNullList[ItemStack]) {
    if (!this.isInCreativeTab(tab)) return
    if(isMekanismGasEnabled)
      getSubItemsGas(subItems)
  }

  @SideOnly(Side.CLIENT)
  @Optional.Method(modid = "MekanismAPI|gas")
  def getSubItemsGas(subItems: NonNullList[ItemStack]){
    import scala.collection.JavaConversions._
    for (gas <- GasRegistry.getRegisteredGasses) {
      val itemStack = new ItemStack(this)
      ItemGas.setGasName(itemStack, gas.getName)
      subItems.add(itemStack)
    }
  }

  override def getItemStackDisplayName(stack: ItemStack): String = {
    if(isMekanismGasEnabled)
      getItemStackDisplayNameGas(stack)
    else
      "null"
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def getItemStackDisplayNameGas(stack: ItemStack): String = {
    val gasName = ItemGas.getGasName(stack)
    if (gasName.isEmpty) return "null"
    val gas = GasRegistry.getGas(gasName)
    if (gas != null) return gas.getLocalizedName
    "null"
  }
}