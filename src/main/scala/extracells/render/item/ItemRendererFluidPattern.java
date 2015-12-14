package extracells.render.item;

import extracells.registries.ItemEnum;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class ItemRendererFluidPattern implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack itemStack, ItemRenderType type) {
		return type != ItemRenderType.ENTITY;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack,
			Object... data) {
		Item item = ItemEnum.FLUIDPATTERN.getItem();
		IIcon fluid = item.getIcon(itemStack, 0);
		IIcon texture = item.getIcon(itemStack, 1);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor3f(1, 1, 1);

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON)
			GL11.glTranslated(0, -10, 5);
		Minecraft.getMinecraft().renderEngine
				.bindTexture(TextureMap.locationBlocksTexture);
		if (fluid != null)
			GuiUtil.drawIcon(fluid, 5, 5, 0, 6, 6);
		Minecraft.getMinecraft().renderEngine
				.bindTexture(TextureMap.locationItemsTexture);
		GL11.glTranslated(0, 0, 0.001F);
		GuiUtil.drawIcon(texture, 0, 0, 0, 16, 16);
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type,
			ItemStack itemStack, ItemRendererHelper helper) {
		return type == ItemRenderType.ENTITY;
	}
}
