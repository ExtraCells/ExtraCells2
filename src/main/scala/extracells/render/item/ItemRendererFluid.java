package extracells.render.item;

import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemRendererFluid implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		Fluid fluid = FluidRegistry.getFluid(item.getItemDamage());
		if (fluid == null)
			return;
		IIcon icon = fluid.getIcon();
		if (icon == null)
			return;
		float f = icon.getMinU();
		float f1 = icon.getMaxU();
		float f2 = icon.getMinV();
		float f3 = icon.getMaxV();
		Minecraft.getMinecraft().renderEngine
				.bindTexture(TextureMap.locationBlocksTexture);
		GuiUtil.drawIcon(icon, 0, 0, 0, 16, 16);
		Minecraft.getMinecraft().renderEngine
				.bindTexture(TextureMap.locationItemsTexture);

	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}

}
