package extracells.item

import java.util
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import extracells.api.ECApi
import extracells.api.IWirelessFluidTermHandler

object ItemWirelessTerminalFluid extends Item with IWirelessFluidTermHandler with WirelessTermBase {
  private[item] var icon: IIcon = null
  override val MAX_POWER: Double =  3200000
  def THIS = this
  setMaxStackSize(1)
  ECApi.instance.registerWirelessFluidTermHandler(this)


  @SuppressWarnings(Array("rawtypes", "unchecked")) override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean) {
    val list2 = list.asInstanceOf[util.List[String]];
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    val encryptionKey: String = itemStack.getTagCompound.getString("key")
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / this.MAX_POWER * 1e4) / 1e2 + "%")
    list2.add(StatCollector.translateToLocal(if (encryptionKey != null && !encryptionKey.isEmpty) "gui.appliedenergistics2.Linked" else "gui.appliedenergistics2.Unlinked"))
  }

  override def getIconFromDamage(dmg: Int): IIcon = {
    return this.icon
  }

  override def getMaxDamage(itemStack: ItemStack): Int = {
    return this.MAX_POWER.toInt
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  override def getSubItems(item: Item, creativeTab: CreativeTabs, itemList: util.List[_]) {
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(item))
    val itemStack: ItemStack = new ItemStack(item)
    injectAEPower(itemStack, this.MAX_POWER)
    itemList2.add(itemStack)
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")
  }

  def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    return getAECurrentPower(is) >= amount
  }

  def isItemNormalWirelessTermToo(is: ItemStack): Boolean = {
    return false
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack = {
    return ECApi.instance.openWirelessTerminal(entityPlayer, itemStack, world)
  }

  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless")
  }

  override def showDurabilityBar(itemStack: ItemStack): Boolean = {
    return true
  }

  def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    extractAEPower(is, amount)
    return true
  }
}
