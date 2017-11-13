package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import extracells.container.ContainerDrive;
import extracells.part.PartDrive;

public class GuiDrive extends GuiContainer {

	private ResourceLocation guiTexture = new ResourceLocation("extracells",
		"textures/gui/drive.png");

	public GuiDrive(PartDrive _part, EntityPlayer _player) {
		super(new ContainerDrive(_part, _player));
		this.xSize = 176;
		this.ySize = 163;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX,
		int sizeY) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop - 18, 0, 0, this.xSize,
			this.ySize);
		for (Object s : this.inventorySlots.inventorySlots) {
			renderBackground((Slot) s);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	private void renderBackground(Slot slot) {
		if ((slot.getStack() == null || slot.getStack().isEmpty()) && slot.slotNumber < 6) {
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(
				new ResourceLocation("appliedenergistics2",
					"textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xPos,
				this.guiTop + slot.yPos, 240, 0, 16, 16);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
		}
	}
}
