package extracells.util

import appeng.api.AEApi
import appeng.api.storage.channels.{IFluidStorageChannel, IItemStorageChannel}
import appeng.api.storage.data.{IAEFluidStack, IAEItemStack}
import extracells.api.gas.{IAEGasStack, IGasStorageChannel}
import extracells.integration.Integration


object StorageChannels {

  val ITEM = AEApi.instance.storage.getStorageChannel[IAEItemStack, IItemStorageChannel](classOf[IItemStorageChannel])

  val FLUID = AEApi.instance.storage.getStorageChannel[IAEFluidStack, IFluidStorageChannel](classOf[IFluidStorageChannel])

  val GAS = if (Integration.Mods.MEKANISMGAS.isEnabled) AEApi.instance.storage.getStorageChannel[IAEGasStack, IGasStorageChannel](classOf[IGasStorageChannel]) else null

}
