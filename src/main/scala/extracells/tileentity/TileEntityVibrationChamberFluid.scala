package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.IGridNode
import appeng.api.networking.energy.IEnergyGrid
import appeng.api.networking.security.IActionHost
import appeng.api.util.{AECableType, AEPartLocation, DimensionalCoord}
import extracells.api.IECTileEntity
import extracells.container.ContainerVibrationChamberFluid
import extracells.gridblock.ECGridBlockVibrantChamber
import extracells.gui.GuiVibrationChamberFluid
import extracells.network.IGuiProvider
import extracells.util.FuelBurnTime
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{capability, _}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

class TileEntityVibrationChamberFluid extends TileBase with IECTileEntity with IActionHost with TPowerStorage with ITickable with IGuiProvider {
  private[tileentity] var isFirstGridNode: Boolean = true
  private final val gridBlock = new ECGridBlockVibrantChamber(this)
  private[tileentity] var node: IGridNode = null
  private var burnTime: Int = 0
  private var burnTimeTotal: Int = 0
  private var timer: Int = 0
  private var timerEnergy: Int = 0
  private var energyLeft: Double = .0
  var tank: FluidTank = new FluidTank((16000)) {
    override def readFromNBT(nbt: NBTTagCompound): FluidTank = {
      if (!nbt.hasKey("Empty")) {
        val fluid: FluidStack = FluidStack.loadFluidStackFromNBT(nbt)
        setFluid(fluid)
      }
      else {
        setFluid(null)
      }
      return this
    }
  }
  var fluidHandler = new FluidHandler

  override def update {
    if (!hasWorldObj) return
    var fluidStack1: FluidStack = tank.getFluid
    if (fluidStack1 != null) fluidStack1 = fluidStack1.copy
    if (worldObj.isRemote) return
    if (burnTime == burnTimeTotal) {
      if (timer >= 40) {
        updateBlock()
        val fluidStack: FluidStack = tank.getFluid
        var bTime: Int = 0
        if (fluidStack != null) bTime = FuelBurnTime.getBurnTime(fluidStack.getFluid)
        else bTime = 0
        if (fluidStack != null && bTime > 0) {
          if (tank.getFluid.amount >= 250) {
            if (energyLeft <= 0) {
              burnTime = 0
              burnTimeTotal = bTime / 4
              tank.drain(250, true)
            }
          }
        }
        timer = 0
      }
      else {
        timer += 1
      }
    }
    else {
      burnTime += 1
      if (timerEnergy == 4) {
        if (energyLeft == 0) {
          val energy: IEnergyGrid = getGridNode(AEPartLocation.INTERNAL).getGrid.getCache(classOf[IEnergyGrid])
          energyLeft = energy.injectPower(24.0D, Actionable.MODULATE)
        }
        else {
          val energy: IEnergyGrid = getGridNode(AEPartLocation.INTERNAL).getGrid.getCache(classOf[IEnergyGrid])
          energyLeft = energy.injectPower(energyLeft, Actionable.MODULATE)
        }
        timerEnergy = 0
      }
      else {
        timerEnergy += 1
      }
    }
    if (fluidStack1 == null && tank.getFluid == null) return
    if (fluidStack1 == null || tank.getFluid == null) {
      updateBlock()
      return
    }
    if (!(fluidStack1 == tank.getFluid)) {
      updateBlock()
      return
    }
    if (fluidStack1.amount != tank.getFluid.amount) {
      updateBlock()
      return
    }
  }

  override def getLocation: DimensionalCoord = new DimensionalCoord(this)

  override def getPowerUsage = 0.0D


  override def getGridNode(forgeDirection: AEPartLocation): IGridNode = {
    if (isFirstGridNode && hasWorldObj && !worldObj.isRemote) {
      isFirstGridNode = false
      try {
        node = AEApi.instance.createGridNode(gridBlock)
        node.updateState
      }
      catch {
        case e: Exception => {
          isFirstGridNode = true
        }
      }
    }
    node
  }

  def getGridNodeWithoutUpdate: IGridNode ={
    if (isFirstGridNode && hasWorldObj && !worldObj.isRemote) {
      isFirstGridNode = false
      try {
        node = AEApi.instance.createGridNode(gridBlock)
      }
      catch {
        case e: Exception => {
          isFirstGridNode = true
        }
      }
    }
    node
  }

  override def getCableConnectionType(forgeDirection: AEPartLocation) = AECableType.SMART

  override def securityBreak {}

  def getTank: FluidTank = tank

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    super.writeToNBT(nbt)
    writePowerToNBT(nbt)
    nbt.setInteger("BurnTime", this.burnTime)
    nbt.setInteger("BurnTimeTotal", this.burnTimeTotal)
    nbt.setInteger("timer", this.timer)
    nbt.setInteger("timerEnergy", this.timerEnergy)
    nbt.setDouble("energyLeft", this.energyLeft)
    tank.writeToNBT(nbt)
    return nbt;
  }

  override def readFromNBT(nbt: NBTTagCompound) {
    super.readFromNBT(nbt)
    readPowerFromNBT(nbt)
    if (nbt.hasKey("BurnTime")) this.burnTime = nbt.getInteger("BurnTime")
    if (nbt.hasKey("BurnTimeTotal")) this.burnTimeTotal = nbt.getInteger("BurnTimeTotal")
    if (nbt.hasKey("timer")) this.timer = nbt.getInteger("timer")
    if (nbt.hasKey("timerEnergy")) this.timerEnergy = nbt.getInteger("timerEnergy")
    if (nbt.hasKey("energyLeft")) this.energyLeft = nbt.getDouble("energyLeft")
    tank.readFromNBT(nbt)
  }


  def getBurntTimeScaled(scal: Int): Int = {
    return if (burnTime != 0) burnTime * scal / burnTimeTotal else 0
  }

  def getActionableNode: IGridNode = {
    return getGridNode(AEPartLocation.INTERNAL)
  }

  override def getUpdateTag: NBTTagCompound = {
    return writeToNBT(new NBTTagCompound)
  }

  def getBurnTime: Int = {
    return burnTime
  }

  def getBurnTimeTotal: Int = {
    return burnTimeTotal
  }

  @SideOnly(Side.CLIENT)
  override def getClientGuiElement(player: EntityPlayer, args: AnyRef*) = new GuiVibrationChamberFluid(player, this)

  override def getServerGuiElement(player: EntityPlayer, args: AnyRef*) = new ContainerVibrationChamberFluid(player.inventory, this)

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
      return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidHandler)
    }
    super.getCapability(capability, facing) }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if(capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY){
      return true
    }
    super.hasCapability(capability, facing)
  }

  protected class FluidHandler extends capability.IFluidHandler{
    override def fill(resource: FluidStack, doFill: Boolean): Int = {
      if (resource == null || resource.getFluid == null || FuelBurnTime.getBurnTime(resource.getFluid) == 0) return 0
      val filled: Int = tank.fill(resource, doFill)
      if (filled != 0 && hasWorldObj) updateBlock()
      filled
    }

    override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = null

    override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = null

    override def getTankProperties: Array[IFluidTankProperties] = tank.getTankProperties
  }
}

