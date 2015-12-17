package extracells.item

import java.util

import appeng.api.config.AccessRestriction
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.StatCollector


trait WirelessTermBase extends PowerItem{

  setMaxStackSize(1)
  override val MAX_POWER: Double = 1600000

  def getPowerFlow(itemStack: ItemStack): AccessRestriction = {
    return AccessRestriction.READ_WRITE
  }


  override def getDurabilityForDisplay(itemStack: ItemStack): Double = {
    return 1 - getAECurrentPower(itemStack) / MAX_POWER
  }

  def canHandle(is: ItemStack): Boolean = {
    if (is == null) return false
    return is.getItem eq this
  }

  def getEncryptionKey(itemStack: ItemStack): String = {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    return itemStack.getTagCompound.getString("key")
  }

  def setEncryptionKey(itemStack: ItemStack, encKey: String, name: String) {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    val tagCompound: NBTTagCompound = itemStack.getTagCompound
    tagCompound.setString("key", encKey)
  }

  def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    return getAECurrentPower(is) >= amount
  }

  def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    extractAEPower(is, amount)
    return true
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  override def getSubItems(item: Item, creativeTab: CreativeTabs, itemList: util.List[_]) {
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(item))
    val itemStack: ItemStack = new ItemStack(item)
    injectAEPower(itemStack, this.MAX_POWER)
    itemList2.add(itemStack)
  }

  override def showDurabilityBar(itemStack: ItemStack): Boolean = {
    return true
  }

  @SuppressWarnings(Array("rawtypes", "unchecked"))
  @SideOnly(Side.CLIENT)
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean) {
    val list2 = list.asInstanceOf[util.List[String]];
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    val encryptionKey: String = itemStack.getTagCompound.getString("key")
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / this.MAX_POWER * 1e4) / 1e2 + "%")
    list2.add(StatCollector.translateToLocal(if (encryptionKey != null && !encryptionKey.isEmpty) "gui.appliedenergistics2.Linked" else "gui.appliedenergistics2.Unlinked"))
  }
}
