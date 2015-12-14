package extracells.gridblock

import java.util.EnumSet

import appeng.api.networking._
import appeng.api.util.{AEColor, DimensionalCoord}
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection


class ECGridBlockHardMEDrive(host: TileEntityHardMeDrive) extends IGridBlock{
  protected var grid: IGrid = null
  protected var usedChannels: Int = 0

  override def getConnectableSides: EnumSet[ForgeDirection] =
    EnumSet.of(ForgeDirection.DOWN, ForgeDirection.UP, ForgeDirection.NORTH, ForgeDirection.EAST, ForgeDirection.SOUTH,
      ForgeDirection.WEST)

  override def getFlags: EnumSet[GridFlags] = EnumSet.of(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY)

  override def getGridColor = AEColor.Transparent

  override def getIdlePowerUsage = host.getPowerUsage

  override def getLocation: DimensionalCoord = host.getLocation

  override def getMachine: IGridHost = host

  override def getMachineRepresentation: ItemStack = {
    val loc: DimensionalCoord = getLocation
    if (loc == null) return null
    new ItemStack(loc.getWorld.getBlock(loc.x, loc.y, loc.z), 1, loc.getWorld.getBlockMetadata(loc.x, loc.y, loc.z))
  }

  override def gridChanged {}

  override def isWorldAccessible = true

  override def onGridNotification(notification: GridNotification) {}

  override def setNetworkStatus(_grid: IGrid, _usedChannels: Int) {
    this.grid = _grid
    this.usedChannels = _usedChannels
  }

}
