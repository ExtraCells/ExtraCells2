package extracells.gui;

import extracells.container.ContainerFluidCrafter;
import extracells.registries.BlockEnum;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiFluidCrafter extends GuiContainer {
	public static final int xSize = 176;
	public static final int ySize = 166;
	private ResourceLocation guiTexture = new ResourceLocation("extracells",
			"textures/gui/fluidcrafter.png");

	public GuiFluidCrafter(InventoryPlayer player, IInventory tileentity) {
		super(new ContainerFluidCrafter(player, tileentity));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
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
		this.fontRendererObj.drawString(BlockEnum.FLUIDCRAFTER.getStatName(),
				5, 5, 0x000000);
	}

	public int getRowLength() {
		return 3;
	}

	private void renderBackground(Slot slot) {
		if (slot.getStack() == null && slot.slotNumber < 9) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(
					new ResourceLocation("appliedenergistics2",
							"textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition,
					this.guiTop + slot.yDisplayPosition, 240, 128, 16, 16);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

}
