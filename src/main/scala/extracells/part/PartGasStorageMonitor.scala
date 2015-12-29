package extracells.part

import appeng.api.parts.IPartHost
import cpw.mods.fml.common.Optional
import extracells.integration.Integration
import extracells.util.{FluidUtil, GasUtil, WrenchUtil}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.{ChatComponentTranslation, Vec3}


class PartGasStorageMonitor extends PartFluidStorageMonitor{
  val isMekEnabled = Integration.Mods.MEKANISMGAS.isEnabled

  override def onActivate(player: EntityPlayer, pos: Vec3): Boolean = {
    if(isMekEnabled)
      onActivateGas(player, pos)
    else
      false
  }

  @Optional.Method(modid = "MekanismAPI|gas")
  def onActivateGas(player: EntityPlayer, pos: Vec3): Boolean = {
    if (player == null || player.worldObj == null) return true
    if (player.worldObj.isRemote) return true
    val s: ItemStack = player.getCurrentEquippedItem
    if (s == null) {
      if (this.locked) return false
      if (this.fluid == null) return true
      if (this.watcher != null) this.watcher.remove(FluidUtil.createAEFluidStack(this.fluid))
      this.fluid = null
      this.amount = 0L
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      return true
    }
    if (WrenchUtil.canWrench(s, player, this.tile.xCoord, this.tile.yCoord, this.tile.zCoord)) {
      this.locked = !this.locked
      WrenchUtil.wrenchUsed(s, player, this.tile.xCoord, this.tile.zCoord, this.tile.yCoord)
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      if (this.locked) player.addChatMessage(new ChatComponentTranslation("chat.appliedenergistics2.isNowLocked"))
      else player.addChatMessage(new ChatComponentTranslation("chat.appliedenergistics2.isNowUnlocked"))
      return true
    }
    if (this.locked) return false
    if (GasUtil.isFilled(s)) {
      if (this.fluid != null && this.watcher != null) this.watcher.remove(FluidUtil.createAEFluidStack(this.fluid))
      val gas = GasUtil.getGasFromContainer(s)
      val fluidStack = GasUtil.getFluidStack(gas)
      this.fluid = {
        if (fluidStack == null)
          null
        else
          fluidStack.getFluid
      }
      if (this.watcher != null) this.watcher.add(FluidUtil.createAEFluidStack(this.fluid))
      val host: IPartHost = getHost
      if (host != null) host.markForUpdate
      return true
    }
    false
  }

}
