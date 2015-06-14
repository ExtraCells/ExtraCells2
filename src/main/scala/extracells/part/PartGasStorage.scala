package extracells.part

import extracells.integration.Integration
import extracells.inventory.HandlerPartStorageGas


class PartGasStorage extends PartFluidStorage{
  handler = {
    if(Integration.Mods.MEKANISMGAS.isEnabled)
      new HandlerPartStorageGas(this)
    else
      handler
  }

}
