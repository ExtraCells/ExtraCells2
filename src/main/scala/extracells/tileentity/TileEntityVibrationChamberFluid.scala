package extracells.tileentity

import appeng.api.AEApi
import appeng.api.config.Actionable
import appeng.api.networking.IGridNode
import appeng.api.networking.energy.IEnergyGrid
import appeng.api.networking.security.IActionHost
import appeng.api.util.{AECableType, DimensionalCoord}
import extracells.api.IECTileEntity
import extracells.gridblock.ECGridBlockVibrantChamber
import extracells.util.FuelBurnTime
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.play.server.S35PacketUpdateTileEntity
import net.minecraft.network.{NetworkManager, Packet}
import net.minecraftforge.common.util.ForgeDirection
import net.minecraftforge.fluids._

class TileEntityVibrationChamberFluid extends TileBase with IECTileEntity with IFluidHandler with IActionHost with TPowerStorage {
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

  override def updateEntity {
    super.updateEntity
    if (!hasWorldObj) return
    var fluidStack1: FluidStack = tank.getFluid
    if (fluidStack1 != null) fluidStack1 = fluidStack1.copy
    if (worldObj.isRemote) return
    if (burnTime == burnTimeTotal) {
      if (timer >= 40) {
        worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord)
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
          val energy: IEnergyGrid = getGridNode(ForgeDirection.UNKNOWN).getGrid.getCache(classOf[IEnergyGrid])
          energyLeft = energy.injectPower(24.0D, Actionable.MODULATE)
        }
        else {
          val energy: IEnergyGrid = getGridNode(ForgeDirection.UNKNOWN).getGrid.getCache(classOf[IEnergyGrid])
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
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
    if (!(fluidStack1 == tank.getFluid)) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
    if (fluidStack1.amount != tank.getFluid.amount) {
      worldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
      return
    }
  }

  override def getLocation: DimensionalCoord = new DimensionalCoord(this)

  override def getPowerUsage = 0.0D


  override def getGridNode(forgeDirection: ForgeDirection): IGridNode = {
    if (isFirstGridNode && hasWorldObj && !getWorldObj.isRemote) {
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
    if (isFirstGridNode && hasWorldObj && !getWorldObj.isRemote) {
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

  override def getCableConnectionType(forgeDirection: ForgeDirection) = AECableType.SMART

  override def securityBreak {}

  override def fill(from: ForgeDirection, resource: FluidStack, doFill: Boolean): Int = {
    if (resource == null || resource.getFluid == null || FuelBurnTime.getBurnTime(resource.getFluid) == 0) return 0
    val filled: Int = tank.fill(resource, doFill)
    if (filled != 0 && hasWorldObj) getWorldObj.markBlockForUpdate(xCoord, yCoord, zCoord)
    filled
  }

  override def drain(from: ForgeDirection, resource: FluidStack, doDrain: Boolean): FluidStack = null


  override def drain(from: ForgeDirection, maxDrain: Int, doDrain: Boolean): FluidStack = null


  override def canFill(from: ForgeDirection, fluid: Fluid): Boolean = {
    if (fluid == null || FuelBurnTime.getBurnTime(fluid) == 0) return false
    true
  }

  override def canDrain(from: ForgeDirection, fluid: Fluid): Boolean = false

  override def getTankInfo(from: ForgeDirection): Array[FluidTankInfo] = Array[FluidTankInfo](tank.getInfo)


  def getTank: FluidTank = tank


  override def writeToNBT(nbt: NBTTagCompound) {
    super.writeToNBT(nbt)
    writePowerToNBT(nbt)
    nbt.setInteger("BurnTime", this.burnTime)
    nbt.setInteger("BurnTimeTotal", this.burnTimeTotal)
    nbt.setInteger("timer", this.timer)
    nbt.setInteger("timerEnergy", this.timerEnergy)
    nbt.setDouble("energyLeft", this.energyLeft)
    tank.writeToNBT(nbt)
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
    return getGridNode(ForgeDirection.UNKNOWN)
  }

  override def getDescriptionPacket: Packet = {
    val nbtTag: NBTTagCompound = new NBTTagCompound
    writeToNBT(nbtTag)
    return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, this.getBlockMetadata, nbtTag)
  }

  override def onDataPacket(net: NetworkManager, pkt: S35PacketUpdateTileEntity) {
    readFromNBT(pkt.func_148857_g)
  }

  def getBurnTime: Int = {
    return burnTime
  }

  def getBurnTimeTotal: Int = {
    return burnTimeTotal
  }
}

