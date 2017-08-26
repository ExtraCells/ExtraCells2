package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import extracells.gui.widget.fluid.WidgetFluidSlot.IConfigurable;
import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.util.FluidHelper;
import extracells.util.NetworkUtil;

public class WidgetSlotFluidContainer extends Gui {

	private int posX, posY;
	private static final ResourceLocation guiTexture = new ResourceLocation(
			"extracells", "textures/gui/busiofluid.png");
	private TileEntityFluidFiller fluidFiller;
	private EntityPlayer player;
	private IConfigurable configurable;
	private byte configOption;

	public WidgetSlotFluidContainer(EntityPlayer _player,
			TileEntityFluidFiller _fluidFiller, int _posX, int _posY) {
		this.player = _player;
		this.fluidFiller = _fluidFiller;
		this.posX = _posX;
		this.posY = _posY;
	}

	public boolean canRender() {
		return this.configurable == null
				|| this.configurable.getConfigState() >= this.configOption;
	}

	public void drawTooltip() {
		if (canRender()) {

		}
	}

	public void drawWidget() {
		ItemStack container = this.fluidFiller.containerItem;
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.zLevel = 100.0F;
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
		itemRender.zLevel = 100.0F;
		FontRenderer font = null;
		if (container != null)
			font = container.getItem().getFontRenderer(container);
		if (font == null)
			font = Minecraft.getMinecraft().fontRendererObj;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRender.renderItemAndEffectIntoGUI(container, this.posX, this.posY);
		// itemRender.renderItemOverlayIntoGUI(font,
		// Minecraft.getMinecraft().getTextureManager(), container, posX + 1,
		// posY - 7, null);
		this.zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	public void drawWidgetWithRect(int i, int j) {
		ItemStack container = this.fluidFiller.containerItem;
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		this.zLevel = 100.0F;
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
		itemRender.zLevel = 100.0F;
		FontRenderer font = null;
		if (container != null)
			font = container.getItem().getFontRenderer(container);
		if (font == null)
			font = Minecraft.getMinecraft().fontRendererObj;
		drawRect(i, j, i + 16, j + 16, -2130706433);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRender.renderItemAndEffectIntoGUI(container, this.posX, this.posY);
		// itemRender.renderItemOverlayIntoGUI(font,
		// Minecraft.getMinecraft().getTextureManager(), container, posX + 1,
		// posY - 7, null);
		this.zLevel = 0.0F;
		itemRender.zLevel = 0.0F;
	}

	public int getPosX() {
		return this.posX;
	}

	public int getPosY() {
		return this.posY;
	}

	public void mouseClicked(ItemStack stack) {
		if (stack != null && stack.getItem() != null && FluidHelper.isEmpty(stack)) {
			NetworkUtil.sendToServer(new PacketFluidContainerSlot(this.fluidFiller, stack));
		}
	}
}
