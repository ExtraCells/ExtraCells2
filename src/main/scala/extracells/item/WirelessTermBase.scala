package extracells.item

import appeng.api.config.AccessRestriction
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


trait WirelessTermBase extends PowerItem{


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

}
