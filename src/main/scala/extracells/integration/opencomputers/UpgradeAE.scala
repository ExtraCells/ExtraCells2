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
import li.cil.oc.api.internal.{Database, Agent, Drone, Robot}
import li.cil.oc.api.machine.{Arguments, Callback, Context}
import li.cil.oc.api.network.{Environment, Node, Visibility}
import li.cil.oc.api.prefab.ManagedEnvironment
import li.cil.oc.integration.appeng.NetworkControl
import li.cil.oc.server.network.Component
import net.minecraft.item.ItemStack
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.FluidContainerRegistry

import scala.math.ScalaNumber


class UpgradeAE(host: EnvironmentHost) extends ManagedEnvironment with NetworkControl[TileSecurity]{
  val robot: Robot =
    if (host.isInstanceOf[Robot])
      host.asInstanceOf[Robot]
    else
      null

  val drone: Drone =
    if (host.isInstanceOf[Drone])
      host.asInstanceOf[Drone]
    else
      null

  val agent: Agent = host.asInstanceOf[Agent]
  setNode(Network.newNode(this, Visibility.Network).withConnector().withComponent("upgrade_me", Visibility.Neighbors).create());

  def getComponent: ItemStack = {
    if (robot != null)
      return robot.getStackInSlot(robot.componentSlot(node.address))
    else if(drone != null){
      val i = drone.internalComponents.iterator
      while (i.hasNext){
        val item = i.next
        if(item != null && item.getItem == ItemUpgradeAE)
          return item
      }
    }
    null
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
      throw new SecurityException("No Security Station")
    val node = sec.getGridNode(ForgeDirection.UNKNOWN)
    if (node == null) throw new SecurityException("No Security Station")
    val gridBlock = node.getGridBlock
    if (gridBlock == null) throw new SecurityException("No Security Station")
    val coord = gridBlock.getLocation
    if (coord == null) throw new SecurityException("No Security Station")
    val tileSecurity = coord.getWorld.getTileEntity(coord.x, coord.y, coord.z).asInstanceOf[TileSecurity]
    if (tileSecurity == null) throw new SecurityException("No Security Station")
    tileSecurity
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
    val selected = agent.selectedSlot
    val invRobot = agent.mainInventory
    if (invRobot.getSizeInventory <= 0) return Array(0.underlying.asInstanceOf[AnyRef])
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

  @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get items from your ae system.")
  def requestItems(context: Context, args: Arguments): Array[AnyRef] = {
    val address = args.checkString(0)
    val entry = args.checkInteger(1)
    val amount = args.optInteger(2, 64)
    val selected = agent.selectedSlot
    val invRobot = agent.mainInventory
    if (invRobot.getSizeInventory <= 0) return Array(0.underlying.asInstanceOf[AnyRef])
    val inv = getItemInventory
    println(inv)
    if (inv == null) return Array(0.underlying.asInstanceOf[AnyRef])
    val n: Node = node.network.node(address)
    if (n == null) throw new IllegalArgumentException("no such component")
    if (!(n.isInstanceOf[Component])) throw new IllegalArgumentException("no such component")
    val component: Component = n.asInstanceOf[Component]
    val env: Environment = n.host
    if (!(env.isInstanceOf[Database])) throw new IllegalArgumentException("not a database")
    val database: Database = env.asInstanceOf[Database]
    val sel = invRobot.getStackInSlot(selected)
    val inSlot =
    if (sel == null)
      0
    else
      sel.stackSize
    val maxSize =
    if (sel == null)
      64
    else
      sel.getMaxStackSize
    val stack = database.getStackInSlot(entry - 1)
    if(stack == null) return Array(0.underlying.asInstanceOf[AnyRef])
    stack.stackSize = Math.min(amount, maxSize - inSlot)
    val stack2 = stack.copy
    stack2.stackSize = 1
    val sel2 =
      if (sel != null) {
        val sel3 = sel.copy
        sel3.stackSize = 1
        sel3
      }else
        null
    if(sel != null && !ItemStack.areItemStacksEqual(sel2, stack2)) return Array(0.underlying.asInstanceOf[AnyRef])
    val extracted = inv.extractItems(AEApi.instance.storage.createItemStack(stack), Actionable.MODULATE, new MachineSource(tile))
    if(extracted == null) return Array(0.underlying.asInstanceOf[AnyRef])
    val ext = extracted.getStackSize.toInt
    stack.stackSize = inSlot + ext
    invRobot.setInventorySlotContents(selected, stack)
    return Array(ext.underlying.asInstanceOf[AnyRef])
  }

  @Callback(doc = "function([number:amount]):number -- Transfer selecte fluid to your ae system.")
  def sendFluids(context: Context, args: Arguments): Array[AnyRef] = {
    val selected = agent.selectedTank
    val tanks = agent.tank
    if (tanks.tankCount <= 0) return Array(0.underlying.asInstanceOf[AnyRef])
    val tank = tanks.getFluidTank(selected)
    val inv = getFluidInventory
    if (tank == null || inv == null || tank.getFluid == null) return Array(0.underlying.asInstanceOf[AnyRef])
    val amount = Math.min(args.optInteger(0, tank.getCapacity), tank.getFluidAmount)
    val fluid = tank.getFluid
    val fluid2 = fluid.copy
    fluid2.amount = amount
    val notInjectet = inv.injectItems(AEApi.instance.storage.createFluidStack(fluid2), Actionable.MODULATE, new MachineSource(tile))
    if (notInjectet == null){
      tank.drain(amount, true)
      return Array(amount.underlying.asInstanceOf[AnyRef])
    }else{
      tank.drain(amount - notInjectet.getStackSize.toInt, true)
      return Array((amount - notInjectet.getStackSize).underlying.asInstanceOf[AnyRef])
    }
  }

  @Callback(doc = "function(database:address, entry:number[, number:amount]):number -- Get fluid from your ae system.")
  def requestFluids(context: Context, args: Arguments): Array[AnyRef] = {
    val address = args.checkString(0)
    val entry = args.checkInteger(1)
    val amount = args.optInteger(2, FluidContainerRegistry.BUCKET_VOLUME)
    val tanks = agent.tank
    val selected = agent.selectedTank
    if (tanks.tankCount <= 0) return Array(0.underlying.asInstanceOf[AnyRef])
    val tank = tanks.getFluidTank(selected)
    val inv = getFluidInventory
    if (tank == null || inv == null) return Array(0.underlying.asInstanceOf[AnyRef])
    val n: Node = node.network.node(address)
    if (n == null) throw new IllegalArgumentException("no such component")
    if (!(n.isInstanceOf[Component])) throw new IllegalArgumentException("no such component")
    val component: Component = n.asInstanceOf[Component]
    val env: Environment = n.host
    if (!(env.isInstanceOf[Database])) throw new IllegalArgumentException("not a database")
    val database: Database = env.asInstanceOf[Database]
    val fluid = FluidContainerRegistry.getFluidForFilledItem(database.getStackInSlot(entry - 1))
    fluid.amount = amount
    val fluid2 = fluid.copy()
    fluid2.amount = tank.fill(fluid, false)
    if (fluid2.amount == 0) return Array(0.underlying.asInstanceOf[AnyRef])
    val extracted = inv.extractItems(AEApi.instance.storage.createFluidStack(fluid2), Actionable.MODULATE, new MachineSource(tile))
    if (extracted == 0) return Array(0.underlying.asInstanceOf[AnyRef])
    return Array(tank.fill(extracted.getFluidStack, true).underlying.asInstanceOf[AnyRef])
  }


  @Callback(doc = "function():boolean -- Return true if the card is linket to your ae network.")
  def isLinked(context: Context, args: Arguments): Array[AnyRef] = {
    val isLinked = getGrid != null
    Array(boolean2Boolean(isLinked))
  }
}
