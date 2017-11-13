package extracells.part.gas

import appeng.api.config.AccessRestriction
import appeng.api.storage.{IMEInventoryHandler, IStorageChannel}
import appeng.api.storage.data.IAEStack
import extracells.inventory.cell.{HandlerPartStorageGas, IHandlerPartBase}
import extracells.part.fluid.PartFluidStorage
import extracells.util.StorageChannels
import net.minecraftforge.fluids.FluidStack
import java.util

import extracells.api.gas.IAEGasStack
import extracells.integration.Integration
import mekanism.api.gas.GasStack
import net.minecraftforge.fml.common.Optional


class PartGasStorage extends PartFluidStorage {
  handler = new HandlerPartStorageGas(this)
  channel = StorageChannels.GAS

  val isMekanismGasEnabled = Integration.Mods.MEKANISMGAS.isEnabled


  var gasList: Map[AnyRef, Int] = Map()

  override def updateNeighbor(){
    if (isMekanismGasEnabled)
      updateNeighborGases()
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  private def updateNeighborGases() {
    gasList = Map()
    if ((access == AccessRestriction.READ) || (access == AccessRestriction.READ_WRITE)) {
      import scala.collection.JavaConversions._
      for (stack <- handler.asInstanceOf[IHandlerPartBase[IAEGasStack]].getAvailableItems(StorageChannels.GAS.createList)) {
        val s = stack.getGasStack.asInstanceOf[GasStack].copy
        gasList += (s -> s.amount)
      }
    }
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  override protected def wasChanged: Boolean = {
    var fluids: Map[AnyRef, Int] = Map()
    import scala.collection.JavaConversions._
    for (stack <- handler.asInstanceOf[IHandlerPartBase[IAEGasStack]].getAvailableItems(StorageChannels.GAS.createList)) {
      val s = stack.getGasStack.asInstanceOf[GasStack].copy
      fluids += (s -> s.amount)
    }
    !fluids.sameElements(gasList)
  }
}
