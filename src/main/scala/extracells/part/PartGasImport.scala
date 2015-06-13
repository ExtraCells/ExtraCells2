package extracells.part

import java.util

import appeng.api.config.Actionable
import appeng.api.storage.data.IAEFluidStack
import cpw.mods.fml.common.Optional
import extracells.integration.Integration
import extracells.util.{FluidUtil, GasUtil}
import mekanism.api.gas.{GasStack, IGasHandler}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.{IFluidHandler, FluidStack, Fluid}



class PartGasImport extends PartFluidImport{

  private val isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled

  override def doWork(rate: Int, TicksSinceLastCall: Int): Boolean = {
    if (getFacingGasTank == null || !isActive || !isMekanismEnabled) return false
    var empty: Boolean = true
    val filter: util.List[Fluid] = new util.ArrayList[Fluid]
    filter.add(this.filterFluids(4))
    if (this.filterSize >= 1) {
      {
        var i: Byte = 1
        while (i < 9) {
          {
            if (i != 4) {
              filter.add(this.filterFluids(i))
            }
          }
          i = (i + 2).toByte
        }
      }
    }
    if (this.filterSize >= 2) {
      {
        var i: Byte = 0
        while (i < 9) {
          {
            if (i != 4) {
              filter.add(this.filterFluids(i))
            }
          }
          i = (i + 2).toByte
        }
      }
    }
    import scala.collection.JavaConversions._
    for (fluid <- filter) {
      if (fluid != null) {
        empty = false
        if (fillToNetwork(fluid, rate * TicksSinceLastCall)) {
          return true
        }
      }
    }
    return empty && fillToNetwork(null, rate * TicksSinceLastCall)
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  override protected def fillToNetwork(fluid: Fluid, toDrain: Int): Boolean = {
    var drained: GasStack = null
    val facingTank: IGasHandler = getFacingGasTank
    val side: ForgeDirection = getSide
    val gasType = {
      if (fluid == null)
        null
      else {
        val gasStack = GasUtil.getGasStack(new FluidStack(fluid, toDrain))
        if (gasStack == null)
          null
        else
          gasStack.getGas
      }
    }
    if (gasType == null) {
      drained = facingTank.drawGas(side.getOpposite, toDrain, false)
    }
    else if (facingTank.canDrawGas(side.getOpposite, gasType)){
      drained = facingTank.drawGas(side.getOpposite, toDrain, false)
    }
    if (drained == null || drained.amount <= 0 || drained.getGas == null) return false
    val toFill: IAEFluidStack = FluidUtil.createAEFluidStack(GasUtil.getFluidStack(drained))
    val notInjected: IAEFluidStack = injectGas(toFill, Actionable.MODULATE)
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
      facingTank.drawGas(side.getOpposite, toFill.getFluidStack.amount, true)
      true
    }
  }
}
