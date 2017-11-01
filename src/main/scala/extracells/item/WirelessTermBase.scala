package extracells.item

import java.util

import appeng.api.config.{AccessRestriction, Actionable}
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.NonNullList
import net.minecraft.util.text.translation.I18n
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}


trait WirelessTermBase extends PowerItem {

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
    extractAEPower(is, amount, Actionable.MODULATE)
    return true
  }

  @SuppressWarnings(Array("unchecked", "rawtypes"))
  override def getSubItems(creativeTab: CreativeTabs, itemList: NonNullList[ItemStack]) {
    if (!this.isInCreativeTab(creativeTab)) return
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(this))
    val itemStack: ItemStack = new ItemStack(this)
    injectAEPower(itemStack, this.MAX_POWER, Actionable.MODULATE)
    itemList2.add(itemStack)
  }

  override def showDurabilityBar(itemStack: ItemStack): Boolean = {
    return true
  }

  @SuppressWarnings(Array("rawtypes", "unchecked"))
  @SideOnly(Side.CLIENT)
  override def addInformation(itemStack: ItemStack, world: World, list: util.List[String], par4: ITooltipFlag) {
    val list2 = list.asInstanceOf[util.List[String]];
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    val encryptionKey: String = itemStack.getTagCompound.getString("key")
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(I18n.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / this.MAX_POWER * 1e4) / 1e2 + "%")
    list2.add(I18n.translateToLocal(if (encryptionKey != null && !encryptionKey.isEmpty) "gui.appliedenergistics2.Linked" else "gui.appliedenergistics2.Unlinked"))
  }
}
