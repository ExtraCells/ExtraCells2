package extracells.gui.fluid;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;

import extracells.container.fluid.ContainerFluidCrafter;
import extracells.registries.BlockEnum;

public class GuiFluidCrafter extends GuiContainer {
	public static final int xSize = 176;
	public static final int ySize = 166;
	private ResourceLocation guiTexture = new ResourceLocation("extracells",
		"textures/gui/fluidcrafter.png");

	public GuiFluidCrafter(InventoryPlayer player, IInventory tileentity) {
		super(new ContainerFluidCrafter(player, tileentity));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
		for (Object s : this.inventorySlots.inventorySlots) {
			renderBackground((Slot) s);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j) {
		this.fontRenderer.drawString(BlockEnum.FLUIDCRAFTER.getStatName(),
			5, 5, 0x000000);
	}

	public int getRowLength() {
		return 3;
	}

	private void renderBackground(Slot slot) {
		if ((slot.getStack() == null || slot.getStack().isEmpty()) && slot.slotNumber < 9) {
			GlStateManager.disableLighting();
			GlStateManager.enableBlend();
			GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(
				new ResourceLocation("appliedenergistics2",
					"textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xPos,
				this.guiTop + slot.yPos, 240, 128, 16, 16);
			GlStateManager.disableBlend();
			GlStateManager.enableLighting();
		}
	}

}
