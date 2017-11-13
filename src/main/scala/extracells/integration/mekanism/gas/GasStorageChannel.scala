package extracells.integration.mekanism.gas

import appeng.api.config.{Actionable, PowerMultiplier}
import appeng.api.networking.energy.IEnergySource
import appeng.api.networking.security.IActionSource
import appeng.api.storage.IMEInventory
import appeng.api.storage.data.IItemList
import extracells.api.gas.{IAEGasStack, IGasStorageChannel}
import extracells.util.GasUtil
import io.netty.buffer.ByteBuf
import mekanism.api.gas.{Gas, GasRegistry, GasStack}
import net.minecraftforge.fluids.{Fluid, FluidStack}


object GasStorageChannel extends IGasStorageChannel {
  override def poweredExtraction(energy: IEnergySource, cell: IMEInventory[IAEGasStack], t: IAEGasStack, source: IActionSource): IAEGasStack = {
    var cellContains = cell.extractItems(t.copy(), Actionable.SIMULATE, source)
    val count = if (cellContains != null) cellContains.getStackSize else 0
    val maxEnergy = energy.extractAEPower(count, Actionable.SIMULATE, PowerMultiplier.CONFIG)
    val toExtract = Math.min(count, maxEnergy.toLong)
    if (toExtract > 0) {
      energy.extractAEPower(count, Actionable.MODULATE, PowerMultiplier.CONFIG)
      cellContains = cellContains.copy().setStackSize(toExtract)
      cell.extractItems(cellContains, Actionable.MODULATE, source)
    } else
      null
  }


  override def readFromPacket(byteBuf: ByteBuf): IAEGasStack = new AEGasStack(byteBuf)

  override def createList(): IItemList[IAEGasStack] = new GasList

  override def poweredInsert(energy: IEnergySource, cell: IMEInventory[IAEGasStack], t: IAEGasStack, source: IActionSource): IAEGasStack = {
    var cellFree = cell.injectItems(t.copy(), Actionable.SIMULATE, source)
    val count = if (cellFree != null) t.getStackSize - cellFree.getStackSize else t.getStackSize
    val maxEnergy = energy.extractAEPower(count, Actionable.SIMULATE, PowerMultiplier.CONFIG)
    val toInsert = Math.min(count, maxEnergy.toLong)
    if (toInsert > 0) {
      energy.extractAEPower(count, Actionable.MODULATE, PowerMultiplier.CONFIG)
      cellFree = cellFree.copy().setStackSize(toInsert)
      cell.injectItems(cellFree, Actionable.MODULATE, source)
    } else
      null
  }

  override def createStack(o: scala.Any): IAEGasStack = {
    o match {
      case gas :Gas => new AEGasStack(new GasStack(gas, 1000))
      case gasStack :GasStack => new AEGasStack(gasStack)
      case gasStack :AEGasStack => new AEGasStack(gasStack)
      case fluid :Fluid => {
        if(GasUtil.isGas(fluid))
          new AEGasStack(GasUtil.getGasStack(new FluidStack(fluid, 1000)))
        else{
          val gas = GasRegistry.getGas(fluid)
          if (gas != null) new AEGasStack(new GasStack(gas, 1000))
          else null
        }
      }
      case fluidStack :FluidStack => {
        if (fluidStack.getFluid == null) null
        else if(GasUtil.isGas(fluidStack)) new AEGasStack(GasUtil.getGasStack(fluidStack))
        else{
          val gas = GasRegistry.getGas(fluidStack.getFluid)
          if (gas != null) new AEGasStack(new GasStack(gas, fluidStack.amount))
          else null
        }
      }
      case _ => null
    }
  }
}
