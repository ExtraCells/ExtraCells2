
package extracells.render.block

import cpw.mods.fml.client.registry.{ISimpleBlockRenderingHandler, RenderingRegistry}
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.{Tessellator, RenderBlocks}
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraft.world.IBlockAccess
import org.lwjgl.opengl.GL11


object RendererHardMEDrive extends ISimpleBlockRenderingHandler {

  var renderID = 0

  val tex = new ResourceLocation("extracells", "textures/blocks/hardmedrive.png")

  def registerRenderer() = {
    renderID = RenderingRegistry.getNextAvailableRenderId
    RenderingRegistry.registerBlockHandler(this)
  }

  override def getRenderId = renderID

  override def shouldRender3DInInventory(modelId: Int): Boolean = true

  override def renderInventoryBlock(block: Block, metadata: Int, modelId: Int, renderer: RenderBlocks) = {
    val tessellator = Tessellator.instance

    renderer.setRenderBounds(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D)
    GL11.glTranslatef(-0.5F, -0.5F, -0.5F)
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, -1.0F, 0.0F)
    renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 0, 3))
    tessellator.draw
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 1.0F, 0.0F)
    renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 1, 3))
    tessellator.draw
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 0.0F, -1.0F)
    renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 2, 3) )
    tessellator.draw
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 0.0F, 1.0F)
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 3, 3))
    tessellator.draw
    //Minecraft.getMinecraft.renderEngine.bindTexture(tex)
    tessellator.startDrawingQuads()
    tessellator.setNormal(0.0F, 0.0F, 1.0F)
    renderer.renderMinX = .25D
    renderer.renderMinY = .25D
    renderer.renderMaxX = .75D
    renderer.renderMaxY = .375D
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, new Icon(5,10,5,7))


    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    tessellator.draw()

    Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
    tessellator.startDrawingQuads
    tessellator.setNormal(-1.0F, 0.0F, 0.0F)
    renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 4, 3))
    tessellator.draw
    tessellator.startDrawingQuads
    tessellator.setNormal(1.0F, 0.0F, 0.0F)
    renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderer.getBlockIconFromSideAndMetadata(block, 5, 3))
    tessellator.draw
    GL11.glTranslatef(0.5F, 0.5F, 0.5F)
  }

  override def renderWorldBlock(world: IBlockAccess, x: Int, y: Int, z: Int, block: Block, modelId: Int, renderer: RenderBlocks): Boolean = {
    renderer.renderStandardBlock(block, x, y, z)


  }


  private class Icon(minU: Float, maxU: Float, minV: Float, maxV: Float) extends IIcon {
    override def getIconHeight: Int = ???

    override def getMinU: Float = minU

    override def getMaxU: Float = maxU

    override def getInterpolatedV (p_94207_1_ : Double):Float  = {
      val f: Float = this.getMaxV - this.getMinV
      this.getMinV + f * (p_94207_1_.toFloat / 16.0F)
    }

    override def getIconName: String = ""

    override def getIconWidth: Int = 0

    override def getMinV: Float = minV

    override def getMaxV: Float = maxV

    override def getInterpolatedU (p_94214_1_ : Double):Float =  {
      val f: Float = this.getMaxU - this.getMinU
      this.getMinU + f * (p_94214_1_.toFloat / 16.0F)
    }
  }
}
