package extracells.part

import extracells.inventory.HandlerPartStorageGas


class PartGasStorage extends PartFluidStorage{
  handler = new HandlerPartStorageGas(this)
}
