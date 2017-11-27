package extracells.integration.appeng

import appeng.api.implementations.items.IAEWrench
import extracells.api.IWrenchHandler
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult

object WrenchHandler extends IWrenchHandler{
  override def canWrench(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Boolean =
    item.getItem.isInstanceOf[IAEWrench] && item.getItem.asInstanceOf[IAEWrench].canWrench(item, user, rayTraceResult.getBlockPos)

  override def wrenchUsed(item: ItemStack, user: EntityPlayer, rayTraceResult: RayTraceResult, hand: EnumHand): Unit = {}
}
