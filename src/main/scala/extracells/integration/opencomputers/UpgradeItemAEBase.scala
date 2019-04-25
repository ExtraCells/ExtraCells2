package extracells.integration.opencomputers

import cpw.mods.fml.common.Optional.{Interface, InterfaceList, Method}
import li.cil.oc.api.CreativeTab
import li.cil.oc.api.driver.item.{HostAware, Slot}
import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.api.internal.{Drone, Robot}
import li.cil.oc.api.network.{Environment, ManagedEnvironment}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

@InterfaceList(Array(
  new Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "OpenComputers", striprefs = true)
))
trait UpgradeItemAEBase extends Item with HostAware{

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
  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment = {
    if (stack != null && stack.getItem == this && worksWith(stack, host.getClass))
      new UpgradeAE(host)
    else
      null
  }

  override def getRarity (stack: ItemStack) : EnumRarity =
    stack.getItemDamage match {
      case 0 => EnumRarity.rare
      case 1 => EnumRarity.uncommon
      case _ => super.getRarity(stack)
  }

  @Method(modid = "OpenComputers")
  override def dataTag(stack: ItemStack) : NBTTagCompound = {
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
  override def worksWith(stack: ItemStack, host: Class[_ <: EnvironmentHost]): Boolean =
    worksWith(stack) && host != null && (classOf[Robot].isAssignableFrom(host) || classOf[Drone].isAssignableFrom(host))

  @InterfaceList(Array(
    new Interface(iface = "li.cil.oc.api.driver.EnvironmentProvider", modid = "OpenComputers", striprefs = true)
  ))
  object Provider extends EnvironmentProvider {
    @Method(modid = "OpenComputers")
    override def getEnvironment(stack: ItemStack): Class[_] =
      if (worksWith(stack))
        classOf[UpgradeAE]
      else null
  }
}
