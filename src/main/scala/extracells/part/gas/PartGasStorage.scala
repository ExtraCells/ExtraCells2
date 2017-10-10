package extracells.part.gas

import extracells.inventory.cell.HandlerPartStorageGas
import extracells.part.fluid.PartFluidStorage


class PartGasStorage extends PartFluidStorage {
  handler = new HandlerPartStorageGas(this)
}
