/*

package extracells.render.block

import cpw.mods.fml.client.registry.{ISimpleBlockRenderingHandler, RenderingRegistry}
import net.minecraft.block.Block
import net.minecraft.client.renderer.RenderBlocks
import net.minecraft.world.IBlockAccess


object RendererHardMEDrive extends ISimpleBlockRenderingHandler {

  var renderID = 0

  def registerRenderer() = {
    renderID = RenderingRegistry.getNextAvailableRenderId
    RenderingRegistry.registerBlockHandler(this)
  }

  override def getRenderId = renderID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks) = renderer.renderBlockAsItem(block, metadata, 1.0F)

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    renderer.renderStandardBlock(block, x, y, z)


  }
}*/
