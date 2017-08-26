package extracells.util;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

	@Nullable
	@SideOnly(Side.CLIENT)
	public static <G> G getGui(Class<G> guiClass) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (guiClass.isInstance(gui)) {
			return guiClass.cast(gui);
		} else {
			return null;
		}
	}

	@Nullable
	@SideOnly(Side.CLIENT)
	public static GuiScreen getGui() {
		return Minecraft.getMinecraft().currentScreen;
	}

	@Nullable
	public static <C> C getContainer(EntityPlayer player, Class<C> containerClass) {
		Container container = player.openContainer;
		if (containerClass.isInstance(container)) {
			return containerClass.cast(container);
		} else {
			return null;
		}
	}
}
