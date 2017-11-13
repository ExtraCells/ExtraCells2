package extracells.part.gas

import extracells.api.gas.IAEGasStack
import extracells.part.fluid.PartFluidLevelEmitter
import extracells.util.{GasUtil, StorageChannels}

class PartGasLevelEmitter extends PartFluidLevelEmitter {

  isGas = true

  override def onStackChangeGas(fullStack: IAEGasStack, diffStack: IAEGasStack): Unit ={
    if (diffStack != null && (diffStack.getGas == GasUtil.getGas(this.selectedFluid))) {
      this.currentAmount = if (fullStack != null) fullStack.getStackSize
      else 0
      val node = getGridNode
      if (node != null) {
        setActive(node.isActive)
        getHost.markForUpdate()
        notifyTargetBlock(getHostTile, getFacing)
      }
    }
  }

}
