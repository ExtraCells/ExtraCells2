package extracells.block

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}


trait TGuiBlock {

  @SideOnly(Side.CLIENT)
  def getClientGuiElement(player: EntityPlayer, world: World, pos: BlockPos) : Any = null

  def getServerGuiElement(player: EntityPlayer, world: World, pos: BlockPos): Any = null

}
