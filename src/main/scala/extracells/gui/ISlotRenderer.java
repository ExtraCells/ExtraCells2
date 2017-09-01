package extracells.gui;

import net.minecraft.inventory.Slot;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ISlotRenderer {

	@SideOnly(Side.CLIENT)
	void renderBackground(Slot slot, GuiBase gui, int mouseX, int mouseY);

	@SideOnly(Side.CLIENT)
	default void renderForeground(Slot slot, GuiBase gui, int mouseX, int mouseY) {

	}
}
