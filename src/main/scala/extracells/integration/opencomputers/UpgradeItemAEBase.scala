package extracells.integration.opencomputers


import li.cil.oc.api.driver.EnvironmentProvider
import li.cil.oc.api.driver.item.{HostAware, Slot}
import li.cil.oc.api.internal.{Drone, Robot}
import li.cil.oc.api.{CreativeTab, network}
import li.cil.oc.api.network.ManagedEnvironment
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.Optional.{Interface, InterfaceList, Method}

@InterfaceList(Array(
  new Interface(iface = "li.cil.oc.api.driver.item.HostAware", modid = "opencomputers", striprefs = true)//,
  //new Interface(iface = "li.cil.oc.api.driver.EnvironmentAware", modid = "opencomputers", striprefs = true)
))
trait UpgradeItemAEBase extends Item with HostAware /*with EnvironmentProvider*/{

  override def getRarity (stack: ItemStack) =
    stack.getItemDamage match {
      case 0 => EnumRarity.RARE
      case 1 => EnumRarity.UNCOMMON
      case _ => super.getRarity(stack)
    }

  @Method(modid = "opencomputers")
  override def tier(stack: ItemStack): Int =
    stack.getItemDamage match {
      case 0 => 2
      case 1 => 1
      case _ => 0
  }

  @Method(modid = "opencomputers")
  override def getCreativeTabs: Array[CreativeTabs] = Array[CreativeTabs](getCreativeTab, CreativeTab.instance)

  @Method(modid = "opencomputers")
  override def slot(stack: ItemStack) = Slot.Upgrade

  @Method(modid = "opencomputers")
  override def worksWith(stack: ItemStack): Boolean = stack != null && stack.getItem == this

  @Method(modid = "opencomputers")
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

  @Method(modid = "opencomputers")
  override def worksWith(stack: ItemStack, host: Class[_ <: network.EnvironmentHost]): Boolean =
    worksWith(stack) && host != null && (classOf[Robot].isAssignableFrom(host) || classOf[Drone].isAssignableFrom(host))

  @Method(modid = "opencomputers")
  override def createEnvironment(stack: ItemStack, host: network.EnvironmentHost): ManagedEnvironment = {
    if (stack != null && stack.getItem == this && worksWith(stack, host.getClass))
      try{
        new UpgradeAEComplete(host)
      }catch{
        case _: Throwable => new UpgradeAE(host)
      }
    else
      null
  }
}
