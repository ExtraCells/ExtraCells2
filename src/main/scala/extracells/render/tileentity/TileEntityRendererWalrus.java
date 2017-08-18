package extracells.render.tileentity;

//TODO: Use model.json
/*public class TileEntityRendererWalrus extends TileEntitySpecialRenderer {

	IModelCustom modelWalrus = AdvancedModelLoader
			.loadModel(new ResourceLocation("extracells", "models/walrus.obj"));
	ResourceLocation textureWalrus = new ResourceLocation("extracells",
			"textures/blocks/walrus.png");

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float partialTickTime) {
		Minecraft.getMinecraft().renderEngine.bindTexture(this.textureWalrus);
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
	}
}*/
