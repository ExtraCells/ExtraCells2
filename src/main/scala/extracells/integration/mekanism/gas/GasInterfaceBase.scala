package extracells.integration.mekanism.gas

import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.security.IActionHost
import appeng.api.util.DimensionalCoord
import cpw.mods.fml.common.Optional.{Interface, InterfaceList, Method}
import extracells.integration.Integration.Mods.MEKANISM
import extracells.network.packet.other.IFluidSlotPartOrBlock
import mekanism.api.gas._
import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids.Fluid

@InterfaceList(Array(
  new Interface(iface = "mekanism.api.gas.IGasHandler", modid = "MekanismAPI|gas", striprefs = true),
  new Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "MekanismAPI|gas", striprefs = true)
))
trait GasInterfaceBase extends IGasHandler with ITubeConnection with IPowerChannelState with IActionHost with IFluidSlotPartOrBlock{

  val isMekanismLoaded = MEKANISM.isEnabled

  @Method(modid = "MekanismAPI|gas")
  def getGasTank(side: ForgeDirection): GasTank

  @Method(modid = "MekanismAPI|gas")
  override def receiveGas(side: ForgeDirection, stack: GasStack, doTransfer: Boolean): Int = getGasTank(side).receive(stack, doTransfer)

  @Method(modid = "MekanismAPI|gas")
  override def drawGas(side: ForgeDirection, amount: Int, doTransfer: Boolean): GasStack = getGasTank(side).draw(amount, doTransfer)

  @Method(modid = "MekanismAPI|gas")
  override def drawGas(side: ForgeDirection, amount: Int): GasStack = drawGas(side, amount, true)

  @Method(modid = "MekanismAPI|gas")
  override def canDrawGas(side: ForgeDirection, gas: Gas): Boolean = getGasTank(side).canDraw(gas)

  @Method(modid = "MekanismAPI|gas")
  override def receiveGas(side: ForgeDirection, stack: GasStack): Int = receiveGas(side, stack, true)

  @Method(modid = "MekanismAPI|gas")
  override def canReceiveGas(side: ForgeDirection, gas: Gas): Boolean = (!hasFilter(side)) && getGasTank(side).canReceive(gas)

  @Method(modid = "MekanismAPI|gas")
  override def canTubeConnect(side: ForgeDirection): Boolean = isMekanismLoaded

  def getFilter(side: ForgeDirection): Int

  def setFilter(side: ForgeDirection, fluid: Fluid): Unit ={
    if(fluid == null)
      setFilter(side, -1)
    else
      setFilter(side, fluid.getID)
  }

  def setFilter(side: ForgeDirection, fluid: Int)

  def hasFilter(side: ForgeDirection) = getFilter(side) != -1

  @Method(modid = "MekanismAPI|gas")
  def exportGas(side: ForgeDirection, gas: GasStack, pos: DimensionalCoord): Int = {
    val tank = getGasTank(side)
    val x = pos.x + side.offsetX
    val y = pos.y + side.offsetY
    val z = pos.z + side.offsetZ
    val world = pos.getWorld
    if(world == null)
      return 0
    val tile = world.getTileEntity(x, y, z)
    if(tile == null)
      return 0
    if(!tile.isInstanceOf[IGasHandler])
      return 0
    val gasHandler = tile.asInstanceOf[IGasHandler]
    if(gasHandler.canReceiveGas(side.getOpposite, gas.getGas))
      gasHandler.receiveGas(side.getOpposite, gas, true)
    else
      0
  }

  override def setFluid(_index: Int, _fluid: Fluid, _player: EntityPlayer) {
    setFilter(ForgeDirection.getOrientation(_index), _fluid)
  }
}
