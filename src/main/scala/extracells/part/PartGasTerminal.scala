package extracells.part

import appeng.api.config.Actionable
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import cpw.mods.fml.common.Optional
import extracells.container.ContainerGasTerminal
import extracells.gridblock.ECBaseGridBlock
import extracells.gui.GuiGasTerminal
import extracells.integration.Integration.Mods
import extracells.util.{GasUtil, FluidUtil}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraftforge.fluids.FluidStack
import org.apache.commons.lang3.tuple.MutablePair


class PartGasTerminal extends PartFluidTerminal{

  val mekLoaded = Mods.MEKANISMGAS.isEnabled

  override protected def isItemValidForInputSlot(i: Int, itemStack: ItemStack): Boolean = {
    GasUtil.isGasContainer(itemStack)
  }

  override def doWork {
    if(mekLoaded)
      doWorkGas
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def doWorkGas {
    val secondSlot: ItemStack = this.inventory.getStackInSlot(1)
    if (secondSlot != null && secondSlot.stackSize >= secondSlot.getMaxStackSize) return
    var container: ItemStack = this.inventory.getStackInSlot(0)
    if (!GasUtil.isGasContainer(container)) return
    container = container.copy
    container.stackSize = 1
    val gridBlock: ECBaseGridBlock = getGridBlock
    if (gridBlock == null) return
    val monitor: IMEMonitor[IAEFluidStack] = gridBlock.getFluidMonitor
    if (monitor == null) return
    if (GasUtil.isEmpty(container)) {
      if (this.currentFluid == null) return
      val capacity: Int = GasUtil.getCapacity(container)
      val result: IAEFluidStack = monitor.extractItems(FluidUtil.createAEFluidStack(this.currentFluid, capacity), Actionable.SIMULATE, this.machineSource)
      val proposedAmount: Int = if (result == null) 0 else Math.min(capacity, result.getStackSize).toInt
      val filledContainer: MutablePair[Integer, ItemStack] = GasUtil.fillStack(container, GasUtil.getGasStack(new FluidStack(this.currentFluid, proposedAmount)))
      if (fillSecondSlot(filledContainer.getRight)) {
        monitor.extractItems(FluidUtil.createAEFluidStack(this.currentFluid, filledContainer.getLeft.toLong), Actionable.MODULATE, this.machineSource)
        decreaseFirstSlot
      }
    }
    else {
      val containerGas = GasUtil.getGasFromContainer(container)
      val notInjected: IAEFluidStack = monitor.injectItems(GasUtil.createAEFluidStack(containerGas), Actionable.SIMULATE, this.machineSource)
      if (notInjected != null) return
      val drainedContainer: MutablePair[Integer, ItemStack] = GasUtil.drainStack(container, containerGas)
      val emptyContainer: ItemStack = drainedContainer.getRight
      if (emptyContainer == null || fillSecondSlot(emptyContainer)) {
        monitor.injectItems(GasUtil.createAEFluidStack(containerGas), Actionable.MODULATE, this.machineSource)
        decreaseFirstSlot
      }
    }
  }
  override def getServerGuiElement(player: EntityPlayer): AnyRef = {
    if (mekLoaded)
      new ContainerGasTerminal(this, player)
    else
      null
  }

  override def getClientGuiElement(player: EntityPlayer): AnyRef = {
    if (mekLoaded)
      new GuiGasTerminal(this, player)
    else
      null
  }
}
