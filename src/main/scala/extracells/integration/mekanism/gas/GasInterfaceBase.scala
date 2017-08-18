package extracells.integration.mekanism.gas

import appeng.api.implementations.IPowerChannelState
import appeng.api.networking.security.IActionHost
import appeng.api.util.DimensionalCoord
import extracells.integration.Integration.Mods.MEKANISM
import extracells.network.packet.other.IFluidSlotPartOrBlock
import mekanism.api.gas._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fml.common.Optional.{Interface, InterfaceList, Method}

@InterfaceList(Array(
  new Interface(iface = "mekanism.api.gas.IGasHandler", modid = "MekanismAPI|gas", striprefs = true),
  new Interface(iface = "mekanism.api.gas.ITubeConnection", modid = "MekanismAPI|gas", striprefs = true)
))
trait GasInterfaceBase extends IGasHandler with ITubeConnection with IPowerChannelState with IActionHost with IFluidSlotPartOrBlock{

  val isMekanismLoaded = MEKANISM.isEnabled

  @Method(modid = "MekanismAPI|gas")
  def getGasTank(side: EnumFacing): GasTank

  @Method(modid = "MekanismAPI|gas")
  override def receiveGas(side: EnumFacing, stack: GasStack, doTransfer: Boolean): Int = getGasTank(side).receive(stack, doTransfer)

  @Method(modid = "MekanismAPI|gas")
  override def drawGas(side: EnumFacing, amount: Int, doTransfer: Boolean): GasStack = getGasTank(side).draw(amount, doTransfer)

  @Method(modid = "MekanismAPI|gas")
  override def canDrawGas(side: EnumFacing, gas: Gas): Boolean = getGasTank(side).canDraw(gas)

  @Method(modid = "MekanismAPI|gas")
  override def canReceiveGas(side: EnumFacing, gas: Gas): Boolean = (!hasFilter(side)) && getGasTank(side).canReceive(gas)

  @Method(modid = "MekanismAPI|gas")
  override def canTubeConnect(side: EnumFacing): Boolean = isMekanismLoaded

  def getFilter(side: EnumFacing): Int

  def setFilter(side: EnumFacing, fluid: Fluid): Unit ={
    if(fluid == null)
      setFilter(side, "")
    else
      setFilter(side, fluid.getName)
  }

  def setFilter(side: EnumFacing, fluid: String)

  def hasFilter(side: EnumFacing) = getFilter(side) != -1

  @Method(modid = "MekanismAPI|gas")
  def exportGas(side: EnumFacing, gas: GasStack, pos: DimensionalCoord): Int = {
    val tank = getGasTank(side)
    val world = pos.getWorld
    if(world == null)
      return 0
    val tile = world.getTileEntity(pos.getPos.offset(side))
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
    setFilter(EnumFacing.getFront(_index), _fluid)
  }
}
