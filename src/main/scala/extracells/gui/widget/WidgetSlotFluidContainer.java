package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

import org.lwjgl.opengl.GL11;

import extracells.network.packet.other.PacketFluidContainerSlot;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.util.FluidHelper;
import extracells.util.NetworkUtil;

public class WidgetSlotFluidContainer extends AbstractWidget {

	private TileEntityFluidFiller fluidFiller;

	public WidgetSlotFluidContainer(TileEntityFluidFiller fluidFiller, WidgetManager manager, int posX, int posY) {
		super(manager, posX, posY);
		this.fluidFiller = fluidFiller;
	}

	@Override
	public void draw(int mouseX, int mouseY) {
		ItemStack container = this.fluidFiller.containerItem;
		GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		manager.gui.setZLevel(100.0F);
		RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();
		itemRender.zLevel = 100.0F;
		/*FontRenderer font = null;
		if (container != null) {
			font = container.getItem().getFontRenderer(container);
		}
		if (font == null) {
			font = Minecraft.getMinecraft().fontRendererObj;
		}*/
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		itemRender.renderItemAndEffectIntoGUI(container, xPos, yPos);
		//itemRender.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), container, posX + 1, posY - 7, null);
		manager.gui.setZLevel(0);
		itemRender.zLevel = 0.0F;
	}

	@Override
	public void drawOverlay(int mouseX, int mouseY) {
		GlStateManager.color(1.0F, 1.0F, 1.0F);
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		manager.gui.drawGradientRect(xPos, yPos, xPos + 16, yPos + 16, -0x7F000001, -0x7F000001);
		GlStateManager.enableDepth();
		GlStateManager.enableLighting();
	}

	@Override
	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
		ItemStack stack = Minecraft.getMinecraft().player.inventory.getItemStack();
		if (stack != null && !stack.isEmpty() && stack.getItem() != null && FluidHelper.isEmpty(stack)) {
			NetworkUtil.sendToServer(new PacketFluidContainerSlot(this.fluidFiller, stack));
		}
	}
}
