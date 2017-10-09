package extracells.part.gas

import appeng.api.parts.IPartHost
import extracells.integration.Integration
import extracells.part.fluid.PartFluidStorageMonitor
import extracells.util.{AEUtils, GasUtil, WrenchUtil}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.Vec3d
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
    if (player == null || player.worldObj == null) return true
    if (player.worldObj.isRemote) return true
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
      if (this.locked) player.addChatMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowLocked"))
      else player.addChatMessage(new TextComponentTranslation("chat.appliedenergistics2.isNowUnlocked"))
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
