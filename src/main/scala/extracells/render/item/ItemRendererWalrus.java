package extracells.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

public class ItemRendererWalrus implements IItemRenderer {

	IModelCustom modelWalrus = AdvancedModelLoader
			.loadModel(new ResourceLocation("extracells", "models/walrus.obj"));
	ResourceLocation textureWalrus = new ResourceLocation("extracells",
			"textures/blocks/walrus.png");

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		Minecraft.getMinecraft().renderEngine.bindTexture(this.textureWalrus);
		GL11.glPushMatrix();
		switch (type) {
		case ENTITY:
			break;
		case EQUIPPED:
			break;
		case EQUIPPED_FIRST_PERSON:
			GL11.glRotated(180, 0, 1, 0);
			GL11.glTranslatef(-1F, 0.5F, -0.5F);
			break;
		case FIRST_PERSON_MAP:
			break;
		case INVENTORY:
			GL11.glTranslatef(-0.5F, -0.5F, -0.1F);
			break;
		default:
			break;
		}
		this.modelWalrus.renderAll();
		GL11.glPopMatrix();
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return true;
	}
}
