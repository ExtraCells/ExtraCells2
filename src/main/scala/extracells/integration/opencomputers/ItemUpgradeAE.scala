package extracells.integration.opencomputers

import cpw.mods.fml.common.registry.GameRegistry
import extracells.Extracells
import extracells.api.ExtraCellsApi
import li.cil.oc.api.driver.EnvironmentHost
import li.cil.oc.api.driver.item.{HostAware, Slot}
import li.cil.oc.api.internal.Robot
import li.cil.oc.api.network.ManagedEnvironment
import net.minecraft.item.{ItemStack, EnumRarity, Item}
import net.minecraft.nbt.NBTTagCompound


object ItemUpgradeAE  extends Item with HostAware{

  setUnlocalizedName("extracells.oc.upgrade")
  setTextureName("extracells:upgrade.oc")

  GameRegistry.registerItem(this, "oc.upgrade", "extracells")

  override def getUnlocalizedName = super.getUnlocalizedName.replace("item.extracells", "extracells.item")

  override def getUnlocalizedName(stack: ItemStack) = getUnlocalizedName

  override def tier(stack: ItemStack): Int = 2

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def worksWith(stack: ItemStack): Boolean = stack != null && stack.getItem == this

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment = {
    if (stack != null && stack.getItem == this && worksWith(stack, host.getClass))
      new UpgradeAE(host)
    else
      null
  }

  override def getRarity (itemStack: ItemStack) = EnumRarity.rare

  override def dataTag(stack: ItemStack) = {
    if (!stack.hasTagCompound) {
      stack.setTagCompound(new NBTTagCompound)
    }
    val nbt: NBTTagCompound = stack.getTagCompound
    if (!nbt.hasKey("oc:data")) {
      nbt.setTag("oc:data", new NBTTagCompound)
    }
    nbt.getCompoundTag("oc:data")
  }

  override def worksWith(stack: ItemStack, host: Class[_ <: EnvironmentHost]): Boolean =
    worksWith(stack) && host != null && classOf[Robot].isAssignableFrom(host)

}
