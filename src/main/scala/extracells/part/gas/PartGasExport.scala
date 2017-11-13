package extracells.part.gas

import java.util

import appeng.api.config.Actionable
import extracells.api.gas.IAEGasStack
import extracells.integration.Integration
import extracells.integration.mekanism.gas.Capabilities
import extracells.part.fluid.PartFluidExport
import extracells.util.StorageChannels
import mekanism.api.gas.{GasStack, IGasHandler, ITubeConnection}
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.{Fluid, FluidStack}
import net.minecraftforge.fml.common.Optional
import net.minecraftforge.fml.common.Optional.Method

@Optional.Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "MekanismAPI|gas", striprefs = true)
class PartGasExport extends PartFluidExport with ITubeConnection {

  private val isMekanismEnabled = Integration.Mods.MEKANISMGAS.isEnabled


  override def doWork(rate: Int, tickSinceLastCall: Int): Boolean = {
    if (isMekanismEnabled)
      work(rate, tickSinceLastCall)
    else
      false
  }


  @Optional.Method(modid = "MekanismAPI|gas")
  protected def work(rate: Int, ticksSinceLastCall: Int): Boolean = {
    val facingTank: IGasHandler = getFacingGasTank
    if (facingTank == null || !isActive) return false
    val filter = new util.ArrayList[Fluid]
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
        val stack: IAEGasStack = extractGas(StorageChannels.GAS.createStack(new FluidStack(fluid, rate * ticksSinceLastCall)), Actionable.SIMULATE)

        if (stack != null) {
          val gasStack = stack.getGasStack.asInstanceOf[GasStack]
          if (gasStack != null && facingTank.canReceiveGas(getFacing.getOpposite, gasStack.getGas)) {
            val filled: Int = facingTank.receiveGas(getFacing.getOpposite, gasStack, true)
            if (filled > 0) {
              extractGas(StorageChannels.GAS.createStack(new FluidStack(fluid, filled)), Actionable.MODULATE)
              return true
            }
          }
        }
      }
    }
    return false
  }

  @Method(modid = "MekanismAPI|gas")
  override def hasCapability(capability: Capability[_]): Boolean = {
      capability == Capabilities.TUBE_CONNECTION_CAPABILITY
  }

  @Method(modid = "MekanismAPI|gas")
  override def getCapability[T](capability: Capability[T]): T = {
    if (capability == Capabilities.TUBE_CONNECTION_CAPABILITY)
      Capabilities.TUBE_CONNECTION_CAPABILITY.cast(this)
    else
      super.getCapability(capability)
  }

  override def canTubeConnect(enumFacing: EnumFacing): Boolean = enumFacing == this.getSide.getFacing


}
