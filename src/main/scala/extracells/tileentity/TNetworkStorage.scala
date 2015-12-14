package extracells.tileentity

import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.{IGrid, IGridHost}
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.{IAEFluidStack, IAEItemStack}
import net.minecraftforge.common.util.ForgeDirection


trait TNetworkStorage {

  def getStorageGrid(side: ForgeDirection): IStorageGrid = {
    if(!this.isInstanceOf[IGridHost])
      return  null
    val host = this.asInstanceOf[IGridHost]
    if (host.getGridNode(side) == null) return null
    val grid: IGrid = host.getGridNode(side).getGrid
    if (grid == null) return null
    grid.getCache(classOf[IStorageGrid])
  }

  def getFluidInventory(side: ForgeDirection): IMEMonitor[IAEFluidStack] = {
    val storageGrid = getStorageGrid(side)
    if (storageGrid == null)
      null
    else
      storageGrid.getFluidInventory
  }

  def getitemInventory(side: ForgeDirection): IMEMonitor[IAEItemStack] = {
    val storageGrid = getStorageGrid(side)
    if (storageGrid == null)
      null
    else
      storageGrid.getItemInventory
  }

}
