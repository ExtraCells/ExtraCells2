package extracells.integration.opencomputers

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.security.MachineSource
import appeng.api.networking.storage.IStorageGrid
import appeng.api.networking.{IGridNode, IGridHost, IGrid}
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.{IAEItemStack, IAEFluidStack}
import appeng.tile.misc.TileSecurity
import li.cil.oc.api.Network
import li.cil.oc.api.driver.EnvironmentHost
import li.cil.oc.api.internal.Robot
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.Visibility
import li.cil.oc.api.prefab.ManagedEnvironment
import li.cil.oc.integration.appeng.NetworkControl
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection

import scala.math.ScalaNumber


class UpgradeAE(host: EnvironmentHost) extends ManagedEnvironment with NetworkControl[TileSecurity]{
  val robot: Robot =
    if (host.isInstanceOf[Robot])
      host.asInstanceOf[Robot]
    else
      null
  setNode(Network.newNode(this, Visibility.Network).withConnector().withComponent("upgrade_me", Visibility.Neighbors).create());

  def getComponent: ItemStack = {
    robot.getStackInSlot(robot.componentSlot(node.address))
  }

  def getSecurity: IGridHost = {
    if (host.world.isRemote) return null
    AEApi.instance.registries.locatable.getLocatableBy(getAEKey).asInstanceOf[IGridHost]
  }

  def getGrid: IGrid = {
    if (host.world.isRemote) return null
    val securityTerminal = getSecurity
    if (securityTerminal == null) return null
    val gridNode: IGridNode = securityTerminal.getGridNode(ForgeDirection.UNKNOWN)
    if (gridNode == null) return null
    gridNode.getGrid
  }

  def getAEKey: Long = {
    try {
      return WirelessHandlerUpgradeAE.getEncryptionKey(getComponent).toLong
    }
    catch {
      case ignored: Throwable => {
      }
    }
    0L
  }


  override def tile: TileSecurity = {
    val sec = getSecurity
    if (sec == null)
      return null
    val node = sec.getGridNode(ForgeDirection.UNKNOWN)
    if (node == null) return null
    val gridBlock = node.getGridBlock
    if (gridBlock == null) return null
    val coord = gridBlock.getLocation
    if (coord == null) return null
    coord.getWorld.getTileEntity(coord.x, coord.y, coord.z).asInstanceOf[TileSecurity]
  }

  def getFluidInventory: IMEMonitor[IAEFluidStack] = {
    val grid = getGrid
    if (grid == null) return null
    val storage: IStorageGrid = grid.getCache(classOf[IStorageGrid])
    if (storage == null) return null
    storage.getFluidInventory
  }

  def getItemInventory: IMEMonitor[IAEItemStack] = {
    val grid = getGrid
    if (grid == null) return null
    val storage: IStorageGrid = grid.getCache(classOf[IStorageGrid])
    if (storage == null) return null
    storage.getItemInventory
  }

  @Callback(doc = "function([number:amount]):number -- Transfer selected items to your ae system.")
  def sendItems(context: Context, args: Arguments): Array[AnyRef] = {
    val selected = robot.selectedSlot
    val invRobot = robot.mainInventory
    val stack = invRobot.getStackInSlot(selected)
    val inv = getItemInventory
    if (stack == null || inv == null) return Array(0.underlying.asInstanceOf[AnyRef])
    val amount = Math.min(args.optInteger(0, 64), stack.stackSize)
    val stack2 = stack.copy
    stack2.stackSize = amount
    val notInjectet = inv.injectItems(AEApi.instance.storage.createItemStack(stack2), Actionable.MODULATE, new MachineSource(tile))
    if (notInjectet == null){
      stack.stackSize -= amount
      if (stack.stackSize <= 0)
        invRobot.setInventorySlotContents(selected, null)
      else
        invRobot.setInventorySlotContents(selected, stack)
      return Array(amount.underlying.asInstanceOf[AnyRef])
    }else{
      stack.stackSize = stack.stackSize - amount + notInjectet.getStackSize.toInt
      if (stack.stackSize <= 0)
        invRobot.setInventorySlotContents(selected, null)
      else
        invRobot.setInventorySlotContents(selected, stack)
      return Array((stack2.stackSize - notInjectet.getStackSize).underlying.asInstanceOf[AnyRef])
    }
  }

  @Callback(doc = "function():boolean -- Return true if the card is linket to your ae network.")
  def isLinked(context: Context, args: Arguments): Array[AnyRef] = {
    val isLinked = getGrid != null
    Array(boolean2Boolean(isLinked))
  }



}
