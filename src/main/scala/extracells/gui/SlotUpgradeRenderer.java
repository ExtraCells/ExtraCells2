package extracells.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

public class SlotUpgradeRenderer implements ISlotRenderer {
	public static final SlotUpgradeRenderer INSTANCE = new SlotUpgradeRenderer();
	private static ResourceLocation TEXTURE_LOCATION = new ResourceLocation("appliedenergistics2", "textures/guis/states.png");

	private SlotUpgradeRenderer() {
	}

	@Override
	public void renderBackground(Slot slot, GuiBase gui, int mouseX, int mouseY) {
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		gui.mc.getTextureManager().bindTexture(TEXTURE_LOCATION);
		gui.drawTexturedModalRect(slot.xPos, slot.yPos, 240, 208, 16, 16);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}
}
