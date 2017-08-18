package extracells.integration.opencomputers

import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.{HostAware, Slot}
import li.cil.oc.api.internal.{Drone, Robot}
import li.cil.oc.api.network.ManagedEnvironment
import li.cil.oc.api.{CreativeTab, network}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.Optional.{Interface, InterfaceList, Method}

/*@InterfaceList(Array(
  new Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "OpenComputers", striprefs = true),
  new Interface(iface = "li.cil.oc.api.driver.EnvironmentAware", modid = "OpenComputers", striprefs = true)
))
trait UpgradeItemAEBase extends Item with HostAware with EnvironmentProvider{

  override def getRarity (stack: ItemStack) =
    stack.getItemDamage match {
      case 0 => EnumRarity.RARE
      case 1 => EnumRarity.UNCOMMON
      case _ => super.getRarity(stack)
    }
  
  @Method(modid = "OpenComputers")
  override def setCreativeTab(creativeTabs: CreativeTabs): Item ={
    super.setCreativeTab(CreativeTab.instance)
  }

  @Method(modid = "OpenComputers")
  override def tier(stack: ItemStack): Int =
    stack.getItemDamage match {
      case 0 => 2
      case 1 => 1
      case _ => 0
  }

  @Method(modid = "OpenComputers")
  override def slot(stack: ItemStack): String = Slot.Upgrade

  @Method(modid = "OpenComputers")
  override def worksWith(stack: ItemStack): Boolean = stack != null && stack.getItem == this

  @Method(modid = "OpenComputers")
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

  @Method(modid = "OpenComputers")
  override def worksWith(stack: ItemStack, host: Class[_ <: network.EnvironmentHost]): Boolean =
    worksWith(stack) && host != null && (classOf[Robot].isAssignableFrom(host) || classOf[Drone].isAssignableFrom(host))

  @Method(modid = "OpenComputers")
  override def getEnvironment(stack: ItemStack): Class[_] = {
    if (stack != null && stack.getItem == this)
      classOf[UpgradeAE]
    else
      null
  }

  @Method(modid = "OpenComputers")
  override def createEnvironment(stack: ItemStack, host: network.EnvironmentHost): ManagedEnvironment = {
    if (stack != null && stack.getItem == this && worksWith(stack, host.getClass))
      new UpgradeAE(host)
    else
      null
  }
}*/
