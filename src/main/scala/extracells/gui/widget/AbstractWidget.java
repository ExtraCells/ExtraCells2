package extracells.gui.widget;

import java.util.Collections;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.gui.IToolTipProvider;

@SideOnly(Side.CLIENT)
public abstract class AbstractWidget implements IToolTipProvider {
	protected final WidgetManager manager;
	protected final int xPos;
	protected final int yPos;
	protected int width = 16;
	protected int height = 16;

	public AbstractWidget(WidgetManager manager, int xPos, int yPos) {
		this.manager = manager;
		this.xPos = xPos;
		this.yPos = yPos;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public abstract void draw(int mouseX, int mouseY);

	/**
	 * Only called if the mouse is over the widget.
	 */
	public void drawOverlay(int mouseX, int mouseY) {
	}

	public List<String> getToolTip(int mouseX, int mouseY) {
		return Collections.emptyList();
	}

	public boolean isMouseOver(int mouseX, int mouseY) {
		return mouseX >= xPos && mouseX </*=*/ xPos + this.width && mouseY >= yPos && mouseY </*=*/ yPos + this.height;
	}

	public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
	}
}
