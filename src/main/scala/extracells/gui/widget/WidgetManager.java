package extracells.gui.widget;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.gui.GuiBase;

@SideOnly(Side.CLIENT)
public class WidgetManager {
	public final GuiBase gui;
	public final Minecraft mc;
	protected final List<AbstractWidget> widgets = new ArrayList<>();

	public WidgetManager(GuiBase gui) {
		this.gui = gui;
		this.mc = Minecraft.getMinecraft();
	}

	public void add(AbstractWidget widget) {
		widgets.add(widget);
	}

	public void remove(AbstractWidget slot) {
		this.widgets.remove(slot);
	}

	public void clear() {
		this.widgets.clear();
	}

	public List<AbstractWidget> getWidgets() {
		return widgets;
	}

	public void drawWidgets(int mouseX, int mouseY) {
		mouseX -= gui.getGuiLeft();
		mouseY -= gui.getGuiTop();
		boolean overlay = false;
		for (AbstractWidget widget : widgets) {
			widget.draw(mouseX, mouseY);
			if (!overlay && widget.isMouseOver(mouseX, mouseY)) {
				widget.drawOverlay(mouseX, mouseY);
				overlay = true;
			}
		}
	}

	@Nullable
	public AbstractWidget getAtPosition(int mX, int mY) {
		for (AbstractWidget slot : widgets) {
			if (slot.isMouseOver(mX, mY)) {
				return slot;
			}
		}
		return null;
	}

	public void handleMouseClicked(int mouseX, int mouseY, int mouseButton) {
		AbstractWidget slot = getAtPosition(mouseX - gui.getGuiLeft(), mouseY - gui.getGuiTop());
		if (slot != null) {
			slot.handleMouseClick(mouseX, mouseY, mouseButton);
		}
	}
}
