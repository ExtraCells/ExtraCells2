package extracells.util;

import net.minecraftforge.fml.client.config.GuiUtils;

import org.lwjgl.opengl.GL11;

import extracells.gui.widget.fluid.WidgetFluidSlot;

public class GuiUtil {

	public static boolean isPointInRegion(float guiLeft, int guiTop, int top,
			int left, int height, int width, int pointX, int pointY) {
		pointX -= guiLeft;
		pointY -= guiTop;
		return pointX >= top - 1 && pointX < top + height + 1
				&& pointY >= left - 1 && pointY < left + width + 1;
	}

	public static boolean renderOverlay(int zLevel, int guiLeft, int guiTop, WidgetFluidSlot fluidSlot, int mouseX, int mouseY) {
		if (isPointInRegion(guiLeft, guiTop, fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			GuiUtils.drawGradientRect(zLevel, fluidSlot.getPosX() + 1,
					fluidSlot.getPosY() + 1, fluidSlot.getPosX() + 17,
					fluidSlot.getPosY() + 17, -0x7F000001, -0x7F000001);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			return true;
		}
		return false;
	}
}
