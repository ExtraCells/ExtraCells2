package extracells.util;

import extracells.gui.widget.fluid.WidgetFluidSlot;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import org.lwjgl.opengl.GL11;

public class GuiUtil {

	public static void drawGradientRect(float zLevel, int par1, int par2,
			int par3, int par4, int par5, int par6) {
		float f = (par5 >> 24 & 255) / 255.0F;
		float f1 = (par5 >> 16 & 255) / 255.0F;
		float f2 = (par5 >> 8 & 255) / 255.0F;
		float f3 = (par5 & 255) / 255.0F;
		float f4 = (par6 >> 24 & 255) / 255.0F;
		float f5 = (par6 >> 16 & 255) / 255.0F;
		float f6 = (par6 >> 8 & 255) / 255.0F;
		float f7 = (par6 & 255) / 255.0F;
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.setColorRGBA_F(f1, f2, f3, f);
		tessellator.addVertex(par3, par2, zLevel);
		tessellator.addVertex(par1, par2, zLevel);
		tessellator.setColorRGBA_F(f5, f6, f7, f4);
		tessellator.addVertex(par1, par4, zLevel);
		tessellator.addVertex(par3, par4, zLevel);
		tessellator.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
	}

	public static void drawIcon(IIcon icon, int x, int y, int z, float width,
			float height) {
		if (icon == null)
			return;
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(x, y + height, z, icon.getMinU(),
				icon.getMaxV());
		tessellator.addVertexWithUV(x + width, y + height, z, icon.getMaxU(),
				icon.getMaxV());
		tessellator.addVertexWithUV(x + width, y, z, icon.getMaxU(),
				icon.getMinV());
		tessellator.addVertexWithUV(x, y, z, icon.getMinU(), icon.getMinV());
		tessellator.draw();
	}

	public static boolean isPointInRegion(float guiLeft, int guiTop, int top,
			int left, int height, int width, int pointX, int pointY) {
		pointX -= guiLeft;
		pointY -= guiTop;
		return pointX >= top - 1 && pointX < top + height + 1
				&& pointY >= left - 1 && pointY < left + width + 1;
	}

	public static boolean renderOverlay(float zLevel, int guiLeft, int guiTop,
			WidgetFluidSlot fluidSlot, int mouseX, int mouseY) {
		if (isPointInRegion(guiLeft, guiTop, fluidSlot.getPosX(),
				fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			drawGradientRect(zLevel, fluidSlot.getPosX() + 1,
					fluidSlot.getPosY() + 1, fluidSlot.getPosX() + 17,
					fluidSlot.getPosY() + 17, -0x7F000001, -0x7F000001);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			return true;
		}
		return false;
	}
}
