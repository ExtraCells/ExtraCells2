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
  override val MAX_POWER: Double =  3200000
  def THIS = this
  ECApi.instance.registerWirelessTermHandler(this)

  override def getIconFromDamage(dmg: Int): IIcon = {
    return this.icon
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")
  }

  def isItemNormalWirelessTermToo(is: ItemStack): Boolean = {
    return false
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack = {
    return ECApi.instance.openWirelessFluidTerminal(entityPlayer, itemStack, world)
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:" + "terminal.fluid.wireless")
  }

}
