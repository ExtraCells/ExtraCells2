package extracells.render.tileentity;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

//TODO: Use model.json
public class TileEntityRendererWalrus extends TileEntitySpecialRenderer {

	/*IModelCustom modelWalrus = AdvancedModelLoader
			.loadModel(new ResourceLocation("extracells", "models/walrus.obj"));*/
	ResourceLocation textureWalrus = new ResourceLocation("extracells", "textures/blocks/walrus.png");

	@Override
	public void renderTileEntityAt(TileEntity te, double x, double y, double z, float partialTicks, int destroyStage) {
		/*Minecraft.getMinecraft().renderEngine.bindTexture(this.textureWalrus);
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		int orientation = tileentity.getBlockMetadata();
		if (orientation == 4) {
			GL11.glRotatef(90, 0, 1, 0);
		} else if (orientation == 5) {
			GL11.glRotatef(-90, 0, 1, 0);
		} else if (orientation == 3) {
			GL11.glRotatef(180, 0, 1, 0);
		}
		this.modelWalrus.renderAll();
		GL11.glPopMatrix();
*/
		GlStateManager.pushMatrix();

		GlStateManager.translate(x + .5, y, z + .5);
		/*long angle = (System.currentTimeMillis() / 10) % 360;
		GlStateManager.rotate(angle, 0, 1, 0);*/

		RenderHelper.disableStandardItemLighting();
		this.bindTexture(textureWalrus);
		if (Minecraft.isAmbientOcclusionEnabled()) {
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
		} else {
			GlStateManager.shadeModel(GL11.GL_FLAT);
		}

		World world = te.getWorld();
		// Translate back to local view coordinates so that we can do the acual rendering here
		GlStateManager.translate(-te.getPos().getX(), -te.getPos().getY(), -te.getPos().getZ());
		Tessellator tessellator = Tessellator.getInstance();
		VertexBuffer bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);

		IBlockState state = te.getWorld().getBlockState(te.getPos());
		BlockRendererDispatcher dispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
		IBakedModel model = dispatcher.getModelForState(state);
		dispatcher.getBlockModelRenderer().renderModel(world, model, state, te.getPos(), bufferBuilder, true);
		tessellator.draw();

		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}
}
