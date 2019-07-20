package extracells.gui;

import extracells.gui.widget.fluid.WidgetFluidSlot;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.Container;

import java.util.ArrayList;
import java.util.List;

abstract class ECGuiContainer extends GuiContainer {
    List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();
    WidgetFluidSlot fluidSlot;

    ECGuiContainer(Container p_i1072_1_) {
        super(p_i1072_1_);
    }

    void showTooltip(int mouseX, int mouseY) {
        if (fluidSlot != null) {
            if (func_146978_c(fluidSlot.getPosX(), fluidSlot.getPosY(), 16, 16, mouseX, mouseY)) {
                fluidSlot.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop);
            }
        }
    }

    void showTooltipList(int mouseX, int mouseY) {
        for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
            if (fluidSlot == null) continue;

            if (func_146978_c(fluidSlot.getPosX(), fluidSlot.getPosY(), 16, 16, mouseX, mouseY)) {
                fluidSlot.drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop);
            }
        }
    }
}
