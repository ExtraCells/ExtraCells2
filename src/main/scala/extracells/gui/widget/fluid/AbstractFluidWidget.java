package extracells.gui.widget.fluid;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public abstract class AbstractFluidWidget extends Gui {

	protected int height = 0, width = 0;
	protected Fluid fluid;
	protected IFluidWidgetGui guiFluidTerminal;

	public AbstractFluidWidget(IFluidWidgetGui guiFluidTerminal, int height,
			int width, Fluid fluid) {
		this.guiFluidTerminal = guiFluidTerminal;
		this.height = height;
		this.width = width;
		this.fluid = fluid;
	}

	@SuppressWarnings("rawtypes")
	protected void drawHoveringText(List list, int x, int y,
			FontRenderer fontrenderer) {
		if (!list.isEmpty()) {
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;

			for (Object string : list) {
				String s = (String) string;
				int l = fontrenderer.getStringWidth(s);

				if (l > k) {
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if (list.size() > 1) {
				k1 += 2 + (list.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4,
					l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1,
					l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3,
					l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3
					- 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1
					+ 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2,
					i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3,
					j2, j2);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				String s1 = (String) list.get(k2);
				fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0) {
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}

	public abstract boolean drawTooltip(int posX, int posY, int mouseX,
			int mouseY);

	public abstract void drawWidget(int posX, int posY);

	public Fluid getFluid() {
		return this.fluid;
	}

	protected boolean isPointInRegion(int top, int left, int height, int width,
			int pointX, int pointY) {
		int k1 = this.guiFluidTerminal.guiLeft();
		int l1 = this.guiFluidTerminal.guiTop() + 18;
		pointX -= k1;
		pointY -= l1;
		return pointX >= top - 1 && pointX < top + height + 1
				&& pointY >= left - 1 && pointY < left + width + 1;
	}

	public abstract void mouseClicked(int posX, int posY, int mouseX, int mouseY);

	public void setFluid(Fluid fluid) {
		this.fluid = fluid;
	}

	public void setFluid(int fluidID) {
		this.fluid = FluidRegistry.getFluid(fluidID);
	}
}
