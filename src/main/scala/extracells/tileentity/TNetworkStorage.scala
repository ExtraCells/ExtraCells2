package extracells.tileentity

import javax.annotation.Nullable

import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.{IGrid, IGridHost}
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.{IAEFluidStack, IAEItemStack}
import appeng.api.util.AEPartLocation


trait TNetworkStorage {

  def getStorageGrid(side: AEPartLocation): IStorageGrid = {
    if(!this.isInstanceOf[IGridHost])
      return  null
    val host = this.asInstanceOf[IGridHost]
    if (host.getGridNode(side) == null) return null
    val grid: IGrid = host.getGridNode(side).getGrid
    if (grid == null) return null
    grid.getCache(classOf[IStorageGrid])
  }

  @Nullable
  def getFluidInventory(side: AEPartLocation): IMEMonitor[IAEFluidStack] = {
    val storageGrid = getStorageGrid(side)
    if (storageGrid == null)
      null
    else
      storageGrid.getFluidInventory
  }

  @Nullable
  def getItemInventory(side: AEPartLocation): IMEMonitor[IAEItemStack] = {
    val storageGrid = getStorageGrid(side)
    if (storageGrid == null)
      null
    else
      storageGrid.getItemInventory
  }

}
