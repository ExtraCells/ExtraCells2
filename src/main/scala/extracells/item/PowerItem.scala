package extracells.item

import appeng.api.config.PowerUnits
import appeng.api.implementations.items.IAEItemPowerStorage
import cofh.api.energy.IEnergyContainerItem
import cpw.mods.fml.common.Optional
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy", striprefs = true)
trait PowerItem extends Item with IAEItemPowerStorage with IEnergyContainerItem{

  val MAX_POWER :Double

  @Optional.Method(modid = "CoFHAPI|energy")
  override def extractEnergy(container: ItemStack, maxExtract: Int, simulate: Boolean): Int = {
    if (container == null) return 0
    if (simulate) {
      return if (getEnergyStored(container) >= maxExtract) maxExtract else getEnergyStored(container)
    }
    else {
      return PowerUnits.AE.convertTo(PowerUnits.RF, extractAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxExtract))).toInt
    }
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override def getEnergyStored(arg0: ItemStack): Int = {
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(arg0)).toInt
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override def getMaxEnergyStored(arg0: ItemStack): Int = {
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(arg0)).toInt
  }

  @Optional.Method(modid = "CoFHAPI|energy")
  override def receiveEnergy(container: ItemStack, maxReceive: Int, simulate: Boolean): Int = {
    if (container == null) return 0
    if (simulate) {
      val current: Double = PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(container))
      val max: Double = PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(container))
      if (max - current >= maxReceive) maxReceive
      else (max - current).toInt
    }
    else {
      val currentAEPower = getAECurrentPower(container)
      if ( currentAEPower < getAEMaxPower(container)){
        PowerUnits.AE.convertTo(PowerUnits.RF, injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive))).toInt
      }else
        0
    }
  }

  override def injectAEPower(itemStack: ItemStack, amt: Double): Double = {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toInject: Double = Math.min(amt, this.MAX_POWER - currentPower)
    tagCompound.setDouble("power", currentPower + toInject)
    return toInject
  }

  override def extractAEPower(itemStack: ItemStack, amt: Double): Double = {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toExtract: Double = Math.min(amt, currentPower)
    tagCompound.setDouble("power", currentPower - toExtract)
    return toExtract
  }

  override def getAECurrentPower(itemStack: ItemStack): Double = {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    return tagCompound.getDouble("power")
  }

  override def getAEMaxPower(itemStack: ItemStack): Double = {
    return this.MAX_POWER
  }

  private def ensureTagCompound(itemStack: ItemStack): NBTTagCompound = {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    return itemStack.getTagCompound
  }

}
