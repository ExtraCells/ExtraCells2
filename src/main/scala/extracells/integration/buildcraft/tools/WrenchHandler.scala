package extracells.integration.buildcraft.tools

import buildcraft.api.tools.IToolWrench
import extracells.api.IWrenchHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult


object WrenchHandler extends IWrenchHandler{
  override def canWrench(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Boolean =
    item.getItem.isInstanceOf[IToolWrench] && item.getItem.asInstanceOf[IToolWrench].canWrench(user, hand, item, rayTraceResult)

  override def wrenchUsed(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Unit =
    item.getItem.asInstanceOf[IToolWrench].wrenchUsed(user, hand, item, rayTraceResult)
}
