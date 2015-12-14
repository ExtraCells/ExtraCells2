package extracells.gui;

import extracells.container.ContainerFluidFiller;
import extracells.gui.widget.WidgetSlotFluidContainer;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;

public class GuiFluidFiller extends GuiContainer {
	public static final int xSize = 176;
	public static final int ySize = 166;
	private ResourceLocation guiTexture = new ResourceLocation("extracells",
			"textures/gui/fluidfiller.png");
	private WidgetSlotFluidContainer fluidContainerSlot;
	private EntityPlayer player;

	public GuiFluidFiller(EntityPlayer player, TileEntityFluidFiller tileentity) {
		super(new ContainerFluidFiller(player.inventory, tileentity));
		this.player = player;
		this.fluidContainerSlot = new WidgetSlotFluidContainer(player,
				tileentity, 80, 35);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRendererObj
				.drawString(
						StatCollector.translateToLocal(
								"extracells.block.fluidfiller.name").replace(
								"ME ", ""), 5, 5, 0x000000);
		int i = this.fluidContainerSlot.getPosX();
		int j = this.fluidContainerSlot.getPosY();
		if (GuiUtil.isPointInRegion(this.guiLeft, this.guiTop, i, j, 16, 16,
				mouseX, mouseY)) {
			this.fluidContainerSlot.drawWidgetWithRect(i, j);
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GL11.glColorMask(true, true, true, false);
			this.drawGradientRect(i, j, i + 16, j + 16, -2130706433,
					-2130706433);
			GL11.glColorMask(true, true, true, true);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
		} else
			this.fluidContainerSlot.drawWidget();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		if (this.fluidContainerSlot != null)
			if (GuiUtil.isPointInRegion(this.guiLeft, this.guiTop,
					this.fluidContainerSlot.getPosX(),
					this.fluidContainerSlot.getPosY(), 18, 18, mouseX, mouseY)) {
				this.fluidContainerSlot.mouseClicked(this.player.inventory
						.getItemStack());

			}
	}
}
