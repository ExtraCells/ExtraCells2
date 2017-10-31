package extracells.part.gas

import appeng.api.config.Actionable
import appeng.api.parts.IPartHost
import appeng.api.storage.IMEMonitor
import appeng.api.storage.data.IAEFluidStack
import extracells.integration.Integration
import extracells.part.fluid.PartFluidConversionMonitor
import extracells.util.{AEUtils, GasUtil, MachineSource, WrenchUtil}
import mekanism.api.gas.IGasItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d
import net.minecraft.util.text.TextComponentTranslation
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.common.Optional
import org.apache.commons.lang3.tuple.MutablePair


class PartGasConversionMonitor extends PartFluidConversionMonitor {

  val isMekEnabled = Integration.Mods.MEKANISMGAS.isEnabled

  override def onActivate(player: EntityPlayer, hand: EnumHand, pos: Vec3d): Boolean = {
    if (isMekEnabled)
      onActivateGas(player, hand, pos)
    else
      false
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def onActivateGas(player: EntityPlayer, hand: EnumHand, pos: Vec3d): Boolean = {
    val b: Boolean = super.onActivate(player, hand, pos)
    if (b) return b
    if (player == null || player.world == null) return true
    if (player.world.isRemote) return true
    val s: ItemStack = player.getHeldItem(hand)
    val mon: IMEMonitor[IAEFluidStack] = getFluidStorage
    if (this.locked && s != null && mon != null) {
      val s2: ItemStack = s.copy
      s2.setCount(1)
      if (GasUtil.isFilled(s2)) {
        val g = GasUtil.getGasFromContainer(s2)
        val f = GasUtil.getFluidStack(g)
        if (f == null) return true
        val fl = AEUtils.createFluidStack(f)
        val not: IAEFluidStack = mon.injectItems(fl.copy, Actionable.SIMULATE, new MachineSource(this))
        if (mon.canAccept(fl) && (not == null || not.getStackSize == 0L)) {
          mon.injectItems(fl, Actionable.MODULATE, new MachineSource(this))
          val empty1: MutablePair[Integer, ItemStack] = GasUtil.drainStack(s2, g)
          val empty: ItemStack = empty1.right
          if (empty != null) {
            dropItems(getHost.getTile.getWorld, getHost.getTile.getPos.offset(getFacing), empty)
          }
          val s3: ItemStack = s.copy
          s3.setCount(s3.getCount - 1)
          if (s3.getCount == 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
          }
          else {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, s3)
          }
        }
        return true
      }
      else if (GasUtil.isEmpty(s2)) {
        if (this.fluid == null) return true
        var extract: IAEFluidStack = null
        if (s2.getItem.isInstanceOf[IGasItem]) {
          extract = mon.extractItems(GasUtil.createAEFluidStack(GasUtil.getGas(this.fluid), (s2.getItem.asInstanceOf[IGasItem]).getMaxGas(s2)), Actionable.SIMULATE, new MachineSource(this))
        }
        else
          return true
        if (extract != null) {
          mon.extractItems(AEUtils.createFluidStack(new FluidStack(this.fluid, extract.getStackSize.toInt)), Actionable.MODULATE, new MachineSource(this))
          val empty1: MutablePair[Integer, ItemStack] = GasUtil.fillStack(s2, GasUtil.getGasStack(extract.getFluidStack))
          if (empty1.left == 0) {
            mon.injectItems(AEUtils.createFluidStack(new FluidStack(this.fluid, extract.getStackSize.toInt)), Actionable.MODULATE, new MachineSource(this))
            return true
          }
          val empty: ItemStack = empty1.right
          if (empty != null) {
            dropItems(getHost.getTile.getWorld, getHost.getTile.getPos.offset(getFacing), empty)
          }
          val s3: ItemStack = s.copy
          s3.setCount(s3.getCount - 1)
          if (s3.getCount == 0) {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, null)
          }
          else {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, s3)
          }
        }
        return true
      }
    }
    false
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def storageMonitor(player: EntityPlayer, hand: EnumHand, pos: Vec3d): Boolean = {
    if (player == null || player.world == null) return true
    if (player.world.isRemote) return true
    val s: ItemStack = player.getHeldItem(hand)
    if (s == null) {
      if (this.locked) return false
      if (this.fluid == null) return true
      if (this.watcher != null) this.watcher.remove(AEUtils.createFluidStack(this.fluid))
      this.fluid = null
      this.amount = 0L
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      return true
    }
    if (WrenchUtil.canWrench(s, player, getHostTile.getPos)) {
      this.locked = !this.locked
      WrenchUtil.wrenchUsed(s, player, getHostTile.getPos)
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      if (this.locked) player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"))
      else player.sendMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"))
      return true
    }
    if (this.locked) return false
    if (GasUtil.isFilled(s)) {
      if (this.fluid != null && this.watcher != null) this.watcher.remove(AEUtils.createFluidStack(this.fluid))
      val gas = GasUtil.getGasFromContainer(s)
      val fluidStack = GasUtil.getFluidStack(gas)
      this.fluid = {
        if (fluidStack == null)
          null
        else
          fluidStack.getFluid
      }
      if (this.watcher != null) this.watcher.add(AEUtils.createFluidStack(this.fluid))
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      return true
    }
    false
  }
}
