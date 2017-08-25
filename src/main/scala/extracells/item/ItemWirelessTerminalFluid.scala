package extracells.item

import extracells.api.{ECApi, IWirelessFluidTermHandler}
import extracells.models.ModelManager
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object ItemWirelessTerminalFluid extends ItemECBase with IWirelessFluidTermHandler with WirelessTermBase {
  def THIS = this

  ECApi.instance.registerWirelessTermHandler(this)


  override def getUnlocalizedName(itemStack: ItemStack): String = super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")


  def isItemNormalWirelessTermToo(is: ItemStack): Boolean = false


  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] =
    new ActionResult(EnumActionResult.SUCCESS, ECApi.instance.openWirelessFluidTerminal(entityPlayer, hand, world))


  @SideOnly(Side.CLIENT)
  override def registerModel(item: Item, manager: ModelManager) =
    manager.registerItemModel(item, 0, "terminals/fluid_wireless")

}
