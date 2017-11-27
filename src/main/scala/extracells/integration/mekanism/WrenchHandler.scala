package extracells.integration.mekanism

import extracells.api.IWrenchHandler
import mekanism.api.IMekWrench
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult


object WrenchHandler extends IWrenchHandler{
  override def canWrench(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Boolean =
    item.getItem.isInstanceOf[IMekWrench] && item.getItem.asInstanceOf[IMekWrench].canUseWrench(item, user, rayTraceResult.getBlockPos)

  override def wrenchUsed(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Unit = {}
}
