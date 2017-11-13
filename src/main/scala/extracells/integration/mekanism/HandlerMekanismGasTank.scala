package extracells.integration.mekanism

import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.IMEInventory
import extracells.api.IExternalGasStorageHandler
import extracells.api.gas.IAEGasStack
import extracells.util.{GasUtil, StorageChannels}
import mekanism.api.gas.{GasStack, GasTank}
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing


object HandlerMekanismGasTank extends IExternalGasStorageHandler {

  val clazz = Class.forName("mekanism.common.tile.TileEntityGasTank")

  override def canHandle(tile: TileEntity, d: EnumFacing, mySrc: IActionSource): Boolean = {
    tile != null && tile.getClass == clazz
  }

  override def getInventory(tile: TileEntity, d: EnumFacing, src: IActionSource): IMEInventory[IAEGasStack] = {
    val tank = getGasTank(tile)
    if (tank == null)
      null
    else
      new Inventory(tank)
  }

  def getGasTank(tile: TileEntity): GasTank = {
    try {
      val tank = clazz.getField("gasTank")
      if (tank != null)
        tank.get(tile).asInstanceOf[GasTank]
      else
        null
    } catch {
      case _: Throwable => null
    }
  }

  class Inventory(tank: GasTank) extends IMEInventory[IAEGasStack] {
    override def injectItems(stackType: IAEGasStack, actionable: Actionable, baseActionSource: IActionSource): IAEGasStack = {
      val gasStack = stackType.getGasStack.asInstanceOf[GasStack]
      if (gasStack == null)
        return stackType
      if (tank.canReceive(gasStack.getGas)) {
        val accepted = tank.receive(gasStack, actionable == Actionable.MODULATE)
        if (accepted == stackType.getStackSize)
          return null
        val returnStack = stackType.copy()
        returnStack.setStackSize(stackType.getStackSize - accepted)
      }
      stackType
    }

    override def getChannel = StorageChannels.GAS

    override def extractItems(stackType: IAEGasStack, actionable: Actionable, baseActionSource: IActionSource): IAEGasStack = {
      val gasStack = stackType.getGasStack.asInstanceOf[GasStack]
      if (gasStack == null)
        return null
      if (tank.canDraw(gasStack.getGas)) {
        val drawed = tank.draw(gasStack.amount, actionable == Actionable.MODULATE)
        return StorageChannels.GAS.createStack(drawed)
      }
      null
    }

    override def getAvailableItems(itemList: IItemList[IAEGasStack]): IItemList[IAEGasStack] = {
      val gas = tank.getGas
      if (gas != null)
        itemList.add(GasUtil.createAEGasStack(gas))
      itemList
    }
  }

}
