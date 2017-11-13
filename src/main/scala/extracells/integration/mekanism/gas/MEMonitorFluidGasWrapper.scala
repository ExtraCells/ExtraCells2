package extracells.integration.mekanism.gas

import java.lang.Iterable
import java.util

import appeng.api.config.{AccessRestriction, Actionable}
import appeng.api.networking.security.IActionSource
import appeng.api.networking.storage.IBaseMonitor
import appeng.api.storage.{IMEMonitor, IMEMonitorHandlerReceiver, IStorageChannel}
import appeng.api.storage.data.{IAEFluidStack, IItemList}
import extracells.api.gas.IAEGasStack
import extracells.util.{GasUtil, StorageChannels}

import scala.collection.mutable.ListBuffer


class MEMonitorFluidGasWrapper(gasMonitor: IMEMonitor[IAEGasStack]) extends IMEMonitor[IAEFluidStack] with IMEMonitorHandlerReceiver[IAEGasStack]{
  final private var listeners: Map[IMEMonitorHandlerReceiver[IAEFluidStack], AnyRef] = Map()

  override def getStorageList: IItemList[IAEFluidStack] = GasUtil.createAEFluidItemList(gasMonitor.getStorageList)

  override def getAvailableItems(iItemList: IItemList[IAEFluidStack]): IItemList[IAEFluidStack] =
    GasUtil.createAEFluidItemList(gasMonitor.getAvailableItems(GasUtil.createAEGasItemList(iItemList)))

  override def isPrioritized(t: IAEFluidStack): Boolean = gasMonitor.isPrioritized(GasUtil.createAEGasStack(t))

  override def getSlot: Int = gasMonitor.getSlot

  override def canAccept(t: IAEFluidStack): Boolean = gasMonitor.canAccept(GasUtil.createAEGasStack(t))

  override def getAccess: AccessRestriction = gasMonitor.getAccess

  override def getPriority: Int = gasMonitor.getPriority

  override def validForPass(i: Int): Boolean = gasMonitor.validForPass(i)

  override def injectItems(t: IAEFluidStack, actionable: Actionable, iActionSource: IActionSource): IAEFluidStack =
    GasUtil.createAEFluidStack(gasMonitor.injectItems(GasUtil.createAEGasStack(t), actionable, iActionSource))

  override def getChannel: IStorageChannel[IAEFluidStack] = StorageChannels.FLUID

  override def extractItems(t: IAEFluidStack, actionable: Actionable, iActionSource: IActionSource): IAEFluidStack = {
    GasUtil.createAEFluidStack(gasMonitor.extractItems(GasUtil.createAEGasStack(t), actionable, iActionSource))
  }

  override def removeListener(l: IMEMonitorHandlerReceiver[IAEFluidStack]): Unit = {
    this.listeners -= l
    if (listeners.size == 0)
      gasMonitor.removeListener(this)
  }

  override def addListener(l: IMEMonitorHandlerReceiver[IAEFluidStack], verificationToken: scala.Any): Unit =
    this.listeners += (l -> verificationToken.asInstanceOf[AnyRef])
    if (listeners.size == 0)
      gasMonitor.addListener(this, null)

  override def onListUpdate(): Unit = {
    for ((key, value) <- this.listeners){
      key.onListUpdate()
    }
  }

  import collection.JavaConverters._
  override def postChange(iBaseMonitor: IBaseMonitor[IAEGasStack], iterable: Iterable[IAEGasStack], iActionSource: IActionSource): Unit = {
    val changes = new ListBuffer[IAEFluidStack]
    for(value <- iterable.asScala){
      changes += GasUtil.createAEFluidStack(value)
    }

    for ((key, value) <- this.listeners){
      key.postChange(this, changes.asJava, iActionSource)
    }
  }

  override def isValid(o: scala.Any): Boolean = true
}
