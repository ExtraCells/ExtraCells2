package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import appeng.api.implementations.ICraftingPatternItem;
import extracells.util.GuiUtil;

public class SlotOutputRenderer implements ISlotRenderer {
	public static final SlotOutputRenderer INSTANCE = new SlotOutputRenderer();

	private SlotOutputRenderer() {
	}

	@Override
	public void renderBackground(Slot slot, GuiBase gui, int mouseX, int mouseY) {
		ItemStack stack = slot.getStack();
		if (stack != null && !stack.isEmpty()) {
			return;
		}
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
		gui.mc.getTextureManager().bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
		gui.drawTexturedModalRect(slot.xPos, slot.yPos, 240, 128, 16, 16);
		GlStateManager.disableBlend();
		GlStateManager.enableLighting();
	}

	@Override
	public void renderForeground(Slot slot, GuiBase gui, int mouseX, int mouseY) {
		ItemStack stack = slot.getStack();
		if (stack == null || !(stack.getItem() instanceof ICraftingPatternItem)) {
			return;
		}
		ICraftingPatternItem pattern = (ICraftingPatternItem) stack.getItem();
		ItemStack output = pattern.getPatternForItem(stack, Minecraft.getMinecraft().world)
			.getCondensedOutputs()[0].createItemStack().copy();

		gui.setZLevel(160.0F);
		GlStateManager.disableLighting();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1, 1, 1);
		GlStateManager.disableLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(gui.textureFile);
		gui.drawTexturedModalRect(slot.xPos,
			slot.yPos, slot.xPos,
			slot.yPos, 18, 18);
		GlStateManager.enableLighting();

		GlStateManager.translate(0.0F, 0.0F, 32.0F);
		gui.setZLevel(150.0F);
		RenderItem itemRender = gui.mc.getRenderItem();
		itemRender.zLevel = 100.0F;
		FontRenderer font = null;
		if (output != null) {
			font = output.getItem().getFontRenderer(output);
		}
		if (font == null) {
			font = Minecraft.getMinecraft().fontRenderer;
		}
		GlStateManager.enableDepth();
		itemRender.renderItemAndEffectIntoGUI(output, slot.xPos, slot.yPos);
		itemRender.renderItemOverlayIntoGUI(font, output, slot.xPos, slot.yPos, null);
		gui.setZLevel(0.0F);
		itemRender.zLevel = 0.0F;

		int i = slot.xPos;
		int j = slot.yPos;
		if (GuiUtil.isPointInRegion(gui.getGuiLeft(), gui.getGuiTop(), i, j, 16, 16, mouseX, mouseY)) {
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			GlStateManager.colorMask(true, true, true, false);
			gui.drawGradientRect(i, j, i + 16, j + 16, -2130706433, -2130706433);
			GlStateManager.colorMask(true, true, true, true);
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}
	}
}
