package extracells.network

import net.minecraft.entity.player.EntityPlayer
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

trait TGuiProvider {

  @SideOnly(Side.CLIENT)
  def getClientGuiElement(player: EntityPlayer, any: Any*) : AnyRef = null

  def getServerGuiElement(player: EntityPlayer, any: Any*): AnyRef = null

}
