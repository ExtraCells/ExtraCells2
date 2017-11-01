package extracells.item

import appeng.api.config.{Actionable, PowerUnits}
import appeng.api.implementations.items.IAEItemPowerStorage
import cofh.redstoneflux.api.IEnergyContainerItem
import net.minecraft.creativetab.CreativeTabs
//import cofh.api.energy.IEnergyContainerItem
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.fml.common.Optional

@Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyContainerItem", modid = "redstoneflux", striprefs = true)
trait PowerItem extends Item with IAEItemPowerStorage with IEnergyContainerItem {

  val MAX_POWER: Double

  @Optional.Method(modid = "redstoneflux")
  override def extractEnergy(container: ItemStack, maxExtract: Int, simulate: Boolean): Int = {
    if (container == null) return 0
    if (simulate) {
      return if (getEnergyStored(container) >= maxExtract) maxExtract else getEnergyStored(container)
    }
    else {
      return PowerUnits.AE.convertTo(PowerUnits.RF, extractAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxExtract), Actionable.MODULATE)).toInt
    }
  }

  @Optional.Method(modid = "redstoneflux")
  override def getEnergyStored(arg0: ItemStack): Int = {
    PowerUnits.RF
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAECurrentPower(arg0)).toInt
  }

  @Optional.Method(modid = "redstoneflux")
  override def getMaxEnergyStored(arg0: ItemStack): Int = {
    return PowerUnits.AE.convertTo(PowerUnits.RF, getAEMaxPower(arg0)).toInt
  }

  @Optional.Method(modid = "redstoneflux")
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
      if (currentAEPower < getAEMaxPower(container)) {
        val leftOver = PowerUnits.AE.convertTo(PowerUnits.RF, injectAEPower(container, PowerUnits.RF.convertTo(PowerUnits.AE, maxReceive), Actionable.MODULATE))
        (maxReceive - leftOver).toInt
      } else
        0
    }
  }

  override def injectAEPower(itemStack: ItemStack, amt: Double, actionable: Actionable): Double = {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toInject: Double = Math.min(amt, this.MAX_POWER - currentPower)
    if (actionable == Actionable.MODULATE)
      tagCompound.setDouble("power", currentPower + toInject)
    toInject
  }

  override def extractAEPower(itemStack: ItemStack, amt: Double, actionable: Actionable): Double = {
    val tagCompound: NBTTagCompound = ensureTagCompound(itemStack)
    val currentPower: Double = tagCompound.getDouble("power")
    val toExtract: Double = Math.min(amt, currentPower)
    if (actionable == Actionable.MODULATE)
      tagCompound.setDouble("power", currentPower - toExtract)
    toExtract
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

  //Workaround for WirelessTermBase
  override def isInCreativeTab(targetTab: CreativeTabs): Boolean = super.isInCreativeTab(targetTab)

}
