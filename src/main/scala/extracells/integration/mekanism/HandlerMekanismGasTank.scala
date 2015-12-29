package extracells.integration.mekanism

import appeng.api.config.Actionable
import appeng.api.networking.security.BaseActionSource
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import appeng.api.storage.{IMEInventory, StorageChannel}
import extracells.api.IExternalGasStorageHandler
import extracells.util.GasUtil
import mekanism.api.gas.GasTank
import net.minecraft.tileentity.TileEntity
import net.minecraftforge.common.util.ForgeDirection


object HandlerMekanismGasTank extends IExternalGasStorageHandler{

  val clazz = Class.forName("mekanism.common.tile.TileEntityGasTank")

  override def canHandle(tile: TileEntity, d: ForgeDirection, mySrc: BaseActionSource): Boolean = {
    tile != null && tile.getClass == clazz
  }

  override def getInventory(tile: TileEntity, d: ForgeDirection, src: BaseActionSource): IMEInventory[IAEFluidStack] = {
    val tank = getGasTank(tile)
    if(tank == null)
      null
    else
      new Inventory(tank)
  }

  def getGasTank(tile: TileEntity): GasTank = {
    try{
      val tank = clazz.getField("gasTank")
      if(tank != null)
        tank.get(tile).asInstanceOf[GasTank]
      else
        null
    } catch {
      case _: Throwable => null
    }
  }

  class Inventory(tank: GasTank) extends IMEInventory[IAEFluidStack]{
    override def injectItems(stackType: IAEFluidStack, actionable: Actionable, baseActionSource: BaseActionSource): IAEFluidStack = {
      val gasStack = GasUtil.getGasStack(stackType)
      if(gasStack == null)
        return stackType
      if(tank.canReceive(gasStack.getGas)){
        val accepted = tank.receive(gasStack, actionable == Actionable.MODULATE)
        if(accepted == stackType.getStackSize)
          return null
        val returnStack = stackType.copy()
        returnStack.setStackSize(stackType.getStackSize - accepted)
      }
      stackType
    }

    override def getChannel: StorageChannel = StorageChannel.FLUIDS

    override def extractItems(stackType: IAEFluidStack, actionable: Actionable, baseActionSource: BaseActionSource): IAEFluidStack = {
      val gasStack = GasUtil.getGasStack(stackType)
      if(gasStack == null)
        return null
      if(tank.canDraw(gasStack.getGas)){
        val drawed = tank.draw(gasStack.amount, actionable == Actionable.MODULATE)
        return GasUtil.createAEFluidStack(drawed)
      }
      null
    }

    override def getAvailableItems(itemList: IItemList[IAEFluidStack]): IItemList[IAEFluidStack] = {
      val gas = tank.getGas
      if (gas != null)
        itemList.add(GasUtil.createAEFluidStack(tank.getGas))
      itemList
    }
  }
}
