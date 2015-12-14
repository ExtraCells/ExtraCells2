
package extracells.render.block

import cpw.mods.fml.client.registry.{ISimpleBlockRenderingHandler, RenderingRegistry}
import extracells.tileentity.TileEntityHardMeDrive
import net.minecraft.block.Block
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureMap
import net.minecraft.client.renderer.{RenderBlocks, Tessellator}
import net.minecraft.util.{IIcon, ResourceLocation}
import net.minecraft.world.IBlockAccess
import org.lwjgl.opengl.GL11


object RendererHardMEDrive extends ISimpleBlockRenderingHandler {

  var renderID = 0

  val tex = new ResourceLocation("extracells", "textures/blocks/hardmedrive.png")

  val i = new Icon(5, 11, 5, 7)
  val i2 = new Icon(5, 11, 8, 10)
  val i3 = new Icon(5, 11, 11, 13)

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
    Minecraft.getMinecraft.renderEngine.bindTexture(tex)
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 0.0F, 1.0F)
    renderer.renderMinX = .3125D
    renderer.renderMinY = .25D
    renderer.renderMaxX = .6875D
    renderer.renderMaxY = .375D
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, i)
    tessellator.draw
    renderer.renderMinY = .43525D
    renderer.renderMaxY = .56025D
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 0.0F, 1.0F)
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, i)
    tessellator.draw
    renderer.renderMinY = .62275D
    renderer.renderMaxY = .75D
    tessellator.startDrawingQuads
    tessellator.setNormal(0.0F, 0.0F, 1.0F)
    renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, i)
    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    tessellator.draw

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
    val tessellator = Tessellator.instance
    renderer.renderStandardBlock(block, x, y, z)
    tessellator.addTranslation(x,  y, z)
    val meta = world.getBlockMetadata(x, y, z)
    val tileEntity = world.getTileEntity(x, y, z)
    if( tileEntity == null || (!tileEntity.isInstanceOf[TileEntityHardMeDrive]))
      return false
    val tileEntityHardMeDrive = tileEntity.asInstanceOf[TileEntityHardMeDrive]

    var b = true
    try {
      Tessellator.instance.draw
    } catch {
      case e: IllegalStateException => b = false
    }

    GL11.glPushMatrix();

    GL11.glEnable(GL11.GL_BLEND);
    GL11.glDisable(GL11.GL_CULL_FACE);
    tessellator.setColorOpaque_I(block.colorMultiplier(world, x, y, z));
    tessellator.setBrightness(240)
    Minecraft.getMinecraft.renderEngine.bindTexture(tex)
    meta match {
      case 2 => renderZNeg(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
      case 3 => renderZPos(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
      case 4 => renderXNeg(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
      case 5 => renderXPos(renderer, block, generateRenderInformations(tileEntityHardMeDrive))
      case _ =>
    }
    Minecraft.getMinecraft.renderEngine.bindTexture(TextureMap.locationBlocksTexture)
    GL11.glPopMatrix();
    if(b)
      tessellator.startDrawingQuads
    tessellator.addTranslation(-x, -y, -z)
    true
  }

  def generateRenderInformations(tileEntity: TileEntityHardMeDrive): Array[RenderInformation] = {
    val renderInformations = new Array[RenderInformation](3)
    renderInformations(2) = new RenderInformation(4, tileEntity.getColorByStatus(2))
    renderInformations(1) = new RenderInformation(7, tileEntity.getColorByStatus(1))
    renderInformations(0) = new RenderInformation(10, tileEntity.getColorByStatus(0))
    renderInformations
  }

  def renderXPos(renderer : RenderBlocks, block: Block, renderInformations: Array[RenderInformation]){
    val tessellator = Tessellator.instance
    renderer.renderMinZ = .3125D
    renderer.renderMaxZ = .6875D
    val it = renderInformations.iterator
    while(it.hasNext){
      val renderInformation = it.next
      renderer.renderMinY = 1.0D / 16.0D * renderInformation.getPos
      renderer.renderMaxY = 1.0D / 16.0D * (renderInformation.getPos + 2)
      tessellator.startDrawingQuads
      tessellator.setNormal(1.0F, 0.0F, 0.0F)
      renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon)
      tessellator.draw
      tessellator.startDrawingQuads
      tessellator.setNormal(1.0F, 0.0F, 0.0F)
      tessellator.setColorOpaque_I(renderInformation.getColor)
      renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon2)
      tessellator.draw
    }
    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMinZ = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    renderer.renderMaxZ = 1.0D
  }

  def renderXNeg(renderer : RenderBlocks, block: Block, renderInformations: Array[RenderInformation]){
    val tessellator = Tessellator.instance
    renderer.renderMinZ = .3125D
    renderer.renderMaxZ = .6875D
    val it = renderInformations.iterator
    while(it.hasNext){
      val renderInformation = it.next
      renderer.renderMinY = 1.0D / 16.0D * renderInformation.getPos
      renderer.renderMaxY = 1.0D / 16.0D * (renderInformation.getPos + 2)
      tessellator.startDrawingQuads
      tessellator.setNormal(-1.0F, 0.0F, 0.0F)
      renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon)
      tessellator.draw
      tessellator.startDrawingQuads
      tessellator.setNormal(-1.0F, 0.0F, 0.0F)
      tessellator.setColorOpaque_I(renderInformation.getColor)
      renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon2)
      tessellator.draw
    }
    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMinZ = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    renderer.renderMaxZ = 1.0D
  }


  def renderZPos(renderer : RenderBlocks, block: Block, renderInformations: Array[RenderInformation]){
    val tessellator = Tessellator.instance
    renderer.renderMinX = .3125D
    renderer.renderMaxX = .6875D
    val it = renderInformations.iterator
    while(it.hasNext){
      val renderInformation = it.next
      renderer.renderMinY = 1.0D / 16.0D * renderInformation.getPos
      renderer.renderMaxY = 1.0D / 16.0D * (renderInformation.getPos + 2.0D)
      tessellator.startDrawingQuads
      tessellator.setNormal(0.0F, 0.0F, 1.0F)
      renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon)
      tessellator.draw
      tessellator.startDrawingQuads
      tessellator.setNormal(0.0F, 0.0F, 1.0F)
      tessellator.setColorOpaque_I(renderInformation.getColor)
      renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon2)
      tessellator.draw
    }
    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMinZ = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    renderer.renderMaxZ = 1.0D
  }

  def renderZNeg(renderer : RenderBlocks, block: Block, renderInformations: Array[RenderInformation]){
    val tessellator = Tessellator.instance
    renderer.renderMinX = .3125D
    renderer.renderMaxX = .6875D
    val it = renderInformations.iterator
    while(it.hasNext){
      val renderInformation = it.next
      renderer.renderMinY = 1.0D / 16.0D * renderInformation.getPos
      renderer.renderMaxY = 1.0D / 16.0D * (renderInformation.getPos + 2.0D)
      tessellator.startDrawingQuads
      tessellator.setNormal(0.0F, 0.0F, -1.0F)
      renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon)
      tessellator.draw
      tessellator.startDrawingQuads
      tessellator.setNormal(0.0F, 0.0F, -1.0F)
      tessellator.setColorOpaque_I(renderInformation.getColor)
      renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, renderInformation.getIcon2)
      tessellator.draw
    }
    renderer.renderMinX = 0.0D
    renderer.renderMinY = 0.0D
    renderer.renderMinZ = 0.0D
    renderer.renderMaxX = 1.0D
    renderer.renderMaxY = 1.0D
    renderer.renderMaxZ = 1.0D
  }

  class RenderInformation(pos: Double, color: Int){
    def getIcon = i3
    def getIcon2 = i3
    def getPos = pos
    def getColor = color
  }


  protected class Icon(minU: Float, maxU: Float, minV: Float, maxV: Float) extends IIcon {
    override def getIconHeight: Int = ???

    override def getMinU: Float = minU

    override def getMaxU: Float = maxU

    override def getInterpolatedV (p_94207_1_ : Double):Float  = {
      val f: Float = this.getMaxV - this.getMinV
      this.getMinV + f// * (p_94207_1_.toFloat / 16.0F)
    }

    override def getIconName: String = ""

    override def getIconWidth: Int = 0

    override def getMinV: Float = minV

    override def getMaxV: Float = maxV

    override def getInterpolatedU (p_94214_1_ : Double):Float =  {
      val f: Float = this.getMaxU - this.getMinU
      this.getMinU + f// * (p_94214_1_.toFloat / 16.0F)
    }
  }
}
