package extracells.util;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class GuiUtil {

	public static boolean isPointInRegion(float guiLeft, int guiTop, int top,
		int left, int height, int width, int pointX, int pointY) {
		pointX -= guiLeft;
		pointY -= guiTop;
		return pointX >= top - 1 && pointX < top + height + 1
			&& pointY >= left - 1 && pointY < left + width + 1;
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

	@Nullable
	public static <C> C getContainer(@Nullable GuiContainer gui, Class<C> containerClass) {
		if (gui == null) {
			return null;
		}
		Container container = gui.inventorySlots;
		if (containerClass.isInstance(container)) {
			return containerClass.cast(container);
		} else {
			return null;
		}
	}
}
