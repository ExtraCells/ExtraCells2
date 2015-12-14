package extracells.gui.widget;

import extracells.gui.widget.fluid.WidgetFluidSlot.IConfigurable;
import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
		RenderItem itemRender = RenderItem.getInstance();
		itemRender.zLevel = 100.0F;
		FontRenderer font = null;
		if (container != null)
			font = container.getItem().getFontRenderer(container);
		if (font == null)
			font = Minecraft.getMinecraft().fontRenderer;
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft()
				.getTextureManager(), container, this.posX, this.posY);
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
		RenderItem itemRender = RenderItem.getInstance();
		itemRender.zLevel = 100.0F;
		FontRenderer font = null;
		if (container != null)
			font = container.getItem().getFontRenderer(container);
		if (font == null)
			font = Minecraft.getMinecraft().fontRenderer;
		drawRect(i, j, i + 16, j + 16, -2130706433);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft()
				.getTextureManager(), container, this.posX, this.posY);
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
		if (stack != null && stack.getItem() != null
				&& FluidUtil.isEmpty(stack))
			new PacketFluidContainerSlot(this.fluidFiller, stack, this.player)
					.sendPacketToServer();
	}
}
