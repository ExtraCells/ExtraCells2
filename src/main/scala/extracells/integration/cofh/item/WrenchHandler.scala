package extracells.integration.cofh.item

import cofh.api.item.IToolHammer
import extracells.api.IWrenchHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult


object WrenchHandler extends IWrenchHandler{
  override def canWrench(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Boolean =
    item.getItem.isInstanceOf[IToolHammer] && item.getItem.asInstanceOf[IToolHammer].isUsable(item, user, rayTraceResult.getBlockPos)

  override def wrenchUsed(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Unit =
    item.getItem.asInstanceOf[IToolHammer].toolUsed(item, user, rayTraceResult.getBlockPos)
}
