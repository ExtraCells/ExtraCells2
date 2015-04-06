package extracells.block

import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.world.World


trait TGuiBlock {

  @SideOnly(Side.CLIENT)
  def getClientGuiElement(player: EntityPlayer, world: World, x: Int, y: Int, z: Int) : Any = null

  def getServerGuiElement(player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any = null

}
