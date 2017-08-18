package extracells.gridblock

import java.util.EnumSet

import appeng.api.networking._
import appeng.api.util.{AEColor, DimensionalCoord}
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumFacing


class ECGridBlockHardMEDrive(host: TileEntityHardMeDrive) extends IGridBlock{
  protected var grid: IGrid = null
  protected var usedChannels: Int = 0

  override def getConnectableSides: EnumSet[EnumFacing] =
    EnumSet.of(EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH,
      EnumFacing.WEST)

  override def getFlags: EnumSet[GridFlags] = EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)

  override def getGridColor = AEColor.TRANSPARENT

  override def getIdlePowerUsage = host.getPowerUsage

  override def getLocation: DimensionalCoord = host.getLocation

  override def getMachine: IGridHost = host

  override def getMachineRepresentation: ItemStack = {
    val loc: DimensionalCoord = getLocation
    if (loc == null) return null
    val blockState: IBlockState = loc.getWorld.getBlockState(loc.getPos)
    new ItemStack(blockState.getBlock, 1, 0)
  }

  override def gridChanged {}

  override def isWorldAccessible = true

  override def onGridNotification(notification: GridNotification) {}

  override def setNetworkStatus(_grid: IGrid, _usedChannels: Int) {
    this.grid = _grid
    this.usedChannels = _usedChannels
  }

}
