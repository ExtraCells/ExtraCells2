package extracells.util

import java.util

import appeng.api.config.AccessRestriction
import appeng.api.config.Actionable
import appeng.api.networking.security.IActionSource
import appeng.api.storage.{IMEInventoryHandler, IMEMonitor, IMEMonitorHandlerReceiver, IStorageChannel}
import appeng.api.storage.data.{IAEStack, IItemList}
import com.google.common.collect.ImmutableList


class MEMonitorHandler[T <: IAEStack[T]](val internalHandler: IMEInventoryHandler[T], val chan: IStorageChannel[T]) extends IMEMonitor[T] {
  final private var cachedList: IItemList[T] = chan.createList()
  final private val listeners: util.HashMap[IMEMonitorHandlerReceiver[T], AnyRef] = new util.HashMap[IMEMonitorHandlerReceiver[T], AnyRef]
  protected var hasChanged: Boolean = true

  def addListener(l: IMEMonitorHandlerReceiver[T], verificationToken: Any) {
    this.listeners.put(l, verificationToken.asInstanceOf[AnyRef])
  }

  def removeListener(l: IMEMonitorHandlerReceiver[T]) {
    this.listeners.remove(l)
  }

  def injectItems(input: T, mode: Actionable, src: IActionSource): T = {
    if (mode eq Actionable.SIMULATE) return this.getHandler.injectItems(input, mode, src)
    this.monitorDifference(input.copy, this.getHandler.injectItems(input, mode, src), false, src)
  }

  protected def getHandler: IMEInventoryHandler[T] = this.internalHandler

  private def monitorDifference(original: T, leftOvers: T, extraction: Boolean, src: IActionSource): T = {

    val diff: T = original.copy
    if (extraction) diff.setStackSize(if (leftOvers == null) 0
    else -leftOvers.getStackSize)
    else if (leftOvers != null) diff.decStackSize(leftOvers.getStackSize)
    import collection.JavaConverters._
    if (diff.getStackSize != 0) this.postChangesToListeners(ImmutableList.of(diff).asScala, src)
    leftOvers
  }

  protected def postChangesToListeners(changes: Iterable[T], src: IActionSource) {
    this.notifyListenersOfChange(changes, src)
  }

  protected def notifyListenersOfChange(diff: Iterable[T], src: IActionSource) {
    this.hasChanged = true // need to update the cache.
    val i: util.Iterator[util.Map.Entry[IMEMonitorHandlerReceiver[T], AnyRef]] = this.getListeners
    import collection.JavaConverters._
    while (i.hasNext) {
      val o: util.Map.Entry[IMEMonitorHandlerReceiver[T], AnyRef] = i.next
      val receiver: IMEMonitorHandlerReceiver[T] = o.getKey
      if (receiver.isValid(o.getValue)) receiver.postChange(this, diff.asJava, src)
      else i.remove()
    }
  }

  protected def getListeners: util.Iterator[util.Map.Entry[IMEMonitorHandlerReceiver[T], AnyRef]] = this.listeners.entrySet.iterator

  def extractItems(request: T, mode: Actionable, src: IActionSource): T = {
    if (mode eq Actionable.SIMULATE) return this.getHandler.extractItems(request, mode, src)
    this.monitorDifference(request.copy, this.getHandler.extractItems(request, mode, src), true, src)
  }

  def getChannel: IStorageChannel[T] = this.getHandler.getChannel

  def getAccess: AccessRestriction = this.getHandler.getAccess

  def getStorageList: IItemList[T] = {
    if (this.hasChanged) {
      this.hasChanged = false
      this.cachedList.resetStatus()
      return this.getAvailableItems(this.cachedList)
    }
    this.cachedList
  }

  def isPrioritized(input: T): Boolean = this.getHandler.isPrioritized(input)

  def canAccept(input: T): Boolean = this.getHandler.canAccept(input)

  def getAvailableItems(out: IItemList[T]): IItemList[T] = this.getHandler.getAvailableItems(out)

  def getPriority: Int = this.getHandler.getPriority

  def getSlot: Int = this.getHandler.getSlot

  def validForPass(i: Int): Boolean = this.getHandler.validForPass(i)
}
