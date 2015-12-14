package extracells.integration.opencomputers

import cpw.mods.fml.common.registry.GameRegistry
import cpw.mods.fml.relauncher.{Side, SideOnly}
import li.cil.oc.api.CreativeTab
import li.cil.oc.api.driver.item.{HostAware, Slot}
import li.cil.oc.api.driver.{EnvironmentAware, EnvironmentHost}
import li.cil.oc.api.internal.{Drone, Robot}
import li.cil.oc.api.network.{Environment, ManagedEnvironment}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound


object ItemUpgradeAE  extends Item with HostAware with EnvironmentAware{

  setUnlocalizedName("extracells.oc.upgrade")
  setTextureName("extracells:upgrade.oc")

  GameRegistry.registerItem(this, "oc.upgrade", "extracells")
  setCreativeTab(CreativeTab.instance)

  override def getUnlocalizedName = super.getUnlocalizedName.replace("item.extracells", "extracells.item")

  override def getUnlocalizedName(stack: ItemStack) = getUnlocalizedName

  override def tier(stack: ItemStack): Int =
    stack.getItemDamage match {
      case 0 => 2
      case 1 => 1
      case _ => 0
  }

  @SideOnly(Side.CLIENT)
  override def getSubItems(item : Item, tab : CreativeTabs, list : java.util.List[_]) {
    def add[T](list: java.util.List[T], value: Any) = list.add(value.asInstanceOf[T])
    add(list, new ItemStack(item, 1, 2))
    add(list, new ItemStack(item, 1, 1))
    add(list, new ItemStack(item, 1, 0))
  }

  override def slot(stack: ItemStack): String = Slot.Upgrade

  override def worksWith(stack: ItemStack): Boolean = stack != null && stack.getItem == this

  override def createEnvironment(stack: ItemStack, host: EnvironmentHost): ManagedEnvironment = {
    if (stack != null && stack.getItem == this && worksWith(stack, host.getClass))
      new UpgradeAE(host)
    else
      null
  }

  override def getRarity (stack: ItemStack) =
    stack.getItemDamage match {
      case 0 => EnumRarity.rare
      case 1 => EnumRarity.uncommon
      case _ => super.getRarity(stack)
  }

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
    worksWith(stack) && host != null && (classOf[Robot].isAssignableFrom(host) || classOf[Drone].isAssignableFrom(host))

  override def providedEnvironment(stack: ItemStack): Class[_ <: Environment] = {
    if (stack != null && stack.getItem == this)
      classOf[UpgradeAE]
    else
      null
  }

  override def getItemStackDisplayName(stack : ItemStack): String = {
    val tier = stack.getItemDamage match {
      case 0 => 3
      case 1 => 2
      case _ => 1
    }
    super.getItemStackDisplayName(stack) + " (Tier " + tier + ")"
  }
}
