package extracells.part.gas

import appeng.api.networking.storage.{IStackWatcher, IStorageGrid}
import appeng.api.parts.IPartHost
import extracells.integration.Integration
import extracells.part.fluid.PartFluidStorageMonitor
import extracells.util.{AEUtils, GasUtil, StorageChannels, WrenchUtil}
import mekanism.api.gas.Gas
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.{RayTraceResult, Vec3d}
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fml.common.Optional


class PartGasStorageMonitor extends PartFluidStorageMonitor {
  val isMekEnabled = Integration.Mods.MEKANISMGAS.isEnabled

  override def onActivate(player: EntityPlayer, hand: EnumHand, pos: Vec3d): Boolean = {
    if (isMekEnabled)
      onActivateGas(player, hand, pos)
    else
      false
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def onActivateGas(player: EntityPlayer, hand: EnumHand, pos: Vec3d): Boolean = {
    if (player == null || player.world == null) return true
    if (player.world.isRemote) return true
    val s: ItemStack = player.getHeldItem(hand)
    if (s == null) {
      if (this.locked) return false
      if (this.fluid == null) return true
      if (this.watcher != null) this.watcher.remove(StorageChannels.GAS.createStack(this.fluid))
      this.fluid = null
      this.amount = 0L
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      return true
    }
    val rayTraceResult = new RayTraceResult(pos, getFacing, this.getLocation.getPos)
    val wrenchHandler = WrenchUtil.getHandler(s, player, rayTraceResult, hand)
    if (wrenchHandler != null) {
      this.locked = !this.locked
      wrenchHandler.wrenchUsed(s, player, rayTraceResult, hand)
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      if (this.locked) player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"))
      else player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"))
      return true
    }
    if (this.locked) return false
    if (GasUtil.isFilled(s)) {
      if (this.fluid != null && this.watcher != null) this.watcher.remove(StorageChannels.GAS.createStack(this.fluid))
      val gas = GasUtil.getGasFromContainer(s)
      val fluidStack = GasUtil.getFluidStack(gas)
      this.fluid = {
        if (fluidStack == null)
          null
        else
          fluidStack.getFluid
      }
      if (this.watcher != null) this.watcher.add(StorageChannels.GAS.createStack(this.fluid))
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      onStackChange()
      return true
    }
    false
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  override protected def onStackChange() {
    if (this.fluid != null) {
      val n = getGridNode
      if (n == null) return
      val g = n.getGrid
      if (g == null) return
      val storage: IStorageGrid = g.getCache(classOf[IStorageGrid])
      if (storage == null) return
      val fluids = getGasStorage
      if (fluids == null) return
      val gas = GasUtil.getGas(this.fluid)
      import scala.collection.JavaConversions._
      for (s <- fluids.getStorageList) {
        if (s.getGas == gas) {
          this.amount = s.getStackSize
          val host = getHost
          if (host != null) host.markForUpdate()
          return
        }
      }
      this.amount = 0L
      val host = getHost
      if (host != null) host.markForUpdate()
    }
  }

  override def updateWatcher(w: IStackWatcher) {
    this.watcher = w
    if (this.fluid != null) w.add(StorageChannels.GAS.createStack(this.fluid))
    onStackChange(null, null, null, null, null)
  }
}
