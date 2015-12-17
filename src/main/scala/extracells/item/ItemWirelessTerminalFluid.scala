package extracells.item

import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.api.{ECApi, IWirelessFluidTermHandler}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.IIcon
import net.minecraft.world.World

object ItemWirelessTerminalFluid extends Item with IWirelessFluidTermHandler with WirelessTermBase {
  private[item] var icon: IIcon = null
  def THIS = this
  ECApi.instance.registerWirelessTermHandler(this)

  override def getIconFromDamage(dmg: Int): IIcon = this.icon


  override def getUnlocalizedName(itemStack: ItemStack): String = super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")


  def isItemNormalWirelessTermToo(is: ItemStack): Boolean = false


  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack =
    ECApi.instance.openWirelessFluidTerminal(entityPlayer, itemStack, world)


  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless")
  }

}
