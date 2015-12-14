package extracells.tileentity

import appeng.api.config.{AccessRestriction, Actionable, PowerMultiplier}
import appeng.api.networking.energy.IAEPowerStorage
import net.minecraft.nbt.NBTTagCompound


trait TPowerStorage extends IAEPowerStorage{

  val powerInformation = new PowerInformation

  override def getAECurrentPower = powerInformation.currentPower

  override def getPowerFlow = AccessRestriction.READ_WRITE

  override def getAEMaxPower = powerInformation.maxPower

  def setMaxPower(power: Double):Unit =  powerInformation.maxPower = power

  override def injectAEPower(amt: Double, mode: Actionable) = {
    val maxStore = powerInformation.maxPower - powerInformation.currentPower
    val notStorred = {
      if(maxStore - amt >= 0)
        0
      else
        amt - maxStore
    }
    if(mode == Actionable.MODULATE)
      powerInformation.currentPower += amt - notStorred
    notStorred
  }

  override def isAEPublicPowerStorage = true

  override def extractAEPower(amount: Double, mode: Actionable, usePowerMultiplier : PowerMultiplier) = {
    val toExtract = Math.min(amount, powerInformation.currentPower)
    if (mode == Actionable.MODULATE)
      powerInformation.currentPower -= toExtract
    toExtract
  }

  def readPowerFromNBT(tag: NBTTagCompound)= {
    if(tag.hasKey("currenPowerBattery"))
      powerInformation.currentPower = tag.getDouble("currenPowerBattery")
  }

  def writePowerToNBT(tag: NBTTagCompound) = tag.setDouble("currenPowerBattery", powerInformation.currentPower)


  class PowerInformation{
    var currentPower = 0.0D

    var maxPower = 500.0D
  }
}
