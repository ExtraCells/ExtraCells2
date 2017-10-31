package extracells.util

import appeng.api.AEApi
import appeng.api.storage.channels.{IFluidStorageChannel, IItemStorageChannel}
import appeng.api.storage.data.{IAEFluidStack, IAEItemStack}


object StorageChannels {

  def ITEM = AEApi.instance().storage().getStorageChannel[IAEItemStack, IItemStorageChannel](classOf[IItemStorageChannel])

  def FLUID = AEApi.instance().storage().getStorageChannel[IAEFluidStack, IFluidStorageChannel](classOf[IFluidStorageChannel])

}
