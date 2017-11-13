package extracells.part.gas

import appeng.api.config.Actionable
import extracells.api.gas.IAEGasStack
import extracells.part.PartECBase
import extracells.util.StorageChannels
import mekanism.api.gas.{Gas, GasStack, IGasHandler}
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.Optional


trait PartGasBase extends PartECBase{

  /*
  @Optional.Method(modid = "MekanismAPI|gas")
  protected def fillGasToNetwork(gas: Gas, toDrain: Int): Boolean = {
    var drained: GasStack = null
    val facingTank: IGasHandler = getFacingGasTank
    val side: EnumFacing = getFacing
    if (gas == null) {
      drained = facingTank.drawGas(side.getOpposite, toDrain, false)
    }
    else if (facingTank.canDrawGas(side.getOpposite, gas)) {
      drained = facingTank.drawGas(side.getOpposite, toDrain, false)
    }
    if (drained == null || drained.amount <= 0 || drained.getGas == null) return false
    val toFill: IAEGasStack = StorageChannels.GAS.createStack(drained)
    val notInjected: IAEGasStack = injectGas(toFill, Actionable.MODULATE)
    if (notInjected != null) {
      val amount: Int = (toFill.getStackSize - notInjected.getStackSize).toInt
      if (amount > 0) {
        facingTank.drawGas(side.getOpposite, amount, true)

        true
      }
      else {
        false
      }
    }
    else {
      facingTank.drawGas(side.getOpposite, toFill.getGasStack.asInstanceOf[GasStack].amount, true)
      true
    }
  }*/

}
