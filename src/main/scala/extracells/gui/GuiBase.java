package extracells.gui;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.client.config.GuiUtils;

import extracells.gui.widget.WidgetManager;

public class GuiBase<C extends Container> extends GuiContainer {
	protected final C container;

	public final ResourceLocation textureFile;
	protected final WidgetManager widgetManager;

	public GuiBase(ResourceLocation texture, C container) {
		super(container);
		this.widgetManager = createWidgetManager();
		this.textureFile = texture;
		this.container = container;
	}

	protected WidgetManager createWidgetManager() {
		return new WidgetManager(this);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		bindTexture(textureFile);
		drawBackground();

		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		GlStateManager.enableRescaleNormal();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.pushMatrix();
		{
			GlStateManager.translate(guiLeft, guiTop, 0.0F);
			widgetManager.drawWidgets(mouseX, mouseY);
		}
		GlStateManager.popMatrix();

		if (hasSlotRenders()) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 0.0F);
			for (Slot slot : container.inventorySlots) {
				ISlotRenderer slotRenderer = getSlotRenderer(slot);
				if (slotRenderer != null) {
					slotRenderer.renderBackground(slot, this, mouseX, mouseY);
				}
			}
			GlStateManager.popMatrix();
		}

		bindTexture(textureFile);
	}

	protected void drawBackground() {
		drawTexturedModalRect(guiLeft + getOffsetX(), guiTop + getOffsetY(), getTextureOffsetX(), getTextureOffsetY(), xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		if (hasSlotRenders()) {
			GlStateManager.pushMatrix();
			//GlStateManager.translate(guiLeft, guiTop, 0.0F);
			for (Slot slot : container.inventorySlots) {
				ISlotRenderer slotRenderer = getSlotRenderer(slot);
				if (slotRenderer != null) {
					slotRenderer.renderForeground(slot, this, mouseX, mouseY);
				}
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
		super.drawGradientRect(left, top, right, bottom, startColor, endColor);
	}

	protected int getOffsetX() {
		return 0;
	}

	protected int getOffsetY() {
		return 0;
	}

	protected int getTextureOffsetX() {
		return 0;
	}

	protected int getTextureOffsetY() {
		return 0;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		drawToolTips(this, buttonList, mouseX, mouseY);
		drawToolTips(this, widgetManager.getWidgets(), mouseX, mouseY);
		renderHoveredToolTip(mouseX, mouseY);
	}

	public static void drawToolTips(GuiBase gui, Collection<?> objects, int mouseX, int mouseY) {
		for (Object obj : objects) {
			if (!(obj instanceof IToolTipProvider)) {
				continue;
			}
			IToolTipProvider provider = (IToolTipProvider) obj;
			List<String> tooltip = provider.getToolTip(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
			if (tooltip == null) {
				continue;
			}
			boolean mouseOver = provider.isMouseOver(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
			if (mouseOver) {
				if (!tooltip.isEmpty()) {
					Minecraft mc = Minecraft.getMinecraft();
					GlStateManager.pushMatrix();
					ScaledResolution scaledresolution = new ScaledResolution(mc);
					GuiUtils.drawHoveringText(tooltip, mouseX, mouseY, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), -1, mc.fontRenderer);
					GlStateManager.popMatrix();
				}
			}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		widgetManager.handleMouseClicked(mouseX, mouseY, mouseButton);
	}

	protected void bindTexture(ResourceLocation texturePath) {
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
		textureManager.bindTexture(texturePath);
	}

	public int getSizeX() {
		return xSize;
	}

	public int getSizeY() {
		return ySize;
	}

	public int getGuiLeft() {
		return guiLeft;
	}

	public int getGuiTop() {
		return guiTop;
	}

	public void setZLevel(float zLevel) {
		this.zLevel = zLevel;
	}

	protected boolean hasSlotRenders() {
		return false;
	}

	@Nullable
	protected ISlotRenderer getSlotRenderer(Slot slot) {
		return null;
	}
}
