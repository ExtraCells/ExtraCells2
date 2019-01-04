package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WidgetFluidTank extends Gui {

	IFluidTank tank;
	public int posX, posY;
	ForgeDirection direction;

	public WidgetFluidTank(IFluidTank tank, int posX, int posY) {
		this(tank, posX, posY, ForgeDirection.UNKNOWN);
	}

	public WidgetFluidTank(IFluidTank tank, int posX, int posY,
			ForgeDirection direction) {
		this.tank = tank;
		this.posX = posX;
		this.posY = posY;
		this.direction = direction;
	}

	public void draw(int guiX, int guiY, int mouseX, int mouseY) {
		if (this.tank == null || 73 < 31)
			return;

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);

		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(
				"extracells", "textures/gui/fluidtank.png"));
		drawTexturedModalRect(this.posX, this.posY, 0, 0, 18, 73);

		int iconHeightRemainder = (73 - 4) % 16;

		FluidStack fluid = this.tank.getFluid();
		if (fluid != null && fluid.amount > 0) {
			Minecraft.getMinecraft().renderEngine
					.bindTexture(TextureMap.locationBlocksTexture);

			IIcon fluidIcon = fluid.getFluid().getStillIcon();
			GL11.glColor3f((fluid.getFluid().getColor() >> 16 & 0xFF) / 255.0F, (fluid.getFluid().getColor() >> 8 & 0xFF) / 255.0F, (fluid.getFluid().getColor() & 0xFF) / 255.0F);
			if (iconHeightRemainder > 0) {
				drawTexturedModelRectFromIcon(this.posX + 1, this.posY + 2,
						fluidIcon, 16, iconHeightRemainder);
			}
			for (int i = 0; i < (73 - 6) / 16; i++) {
				drawTexturedModelRectFromIcon(this.posX + 1, this.posY + 2 + i
						* 16 + iconHeightRemainder, fluidIcon, 16, 16);
			}
			GL11.glColor3f(1.0F, 1.0F, 1.0F);
			Minecraft.getMinecraft().renderEngine
					.bindTexture(new ResourceLocation("extracells",
							"textures/gui/fluidtank.png"));
			drawTexturedModalRect(this.posX + 2, this.posY + 1, 1, 1, 15,
					72 - (int) (73 * ((float) fluid.amount / this.tank
							.getCapacity())));
		}

		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(
				"extracells", "textures/gui/fluidtank.png"));
		drawTexturedModalRect(this.posX + 1, this.posY + 1, 19, 1, 16, 73);

		GL11.glEnable(GL11.GL_LIGHTING);
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
			Iterator iterator = list.iterator();

			while (iterator.hasNext()) {
				String s = (String) iterator.next();
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

	public void drawDirectionTooltip(int x, int y) {

		List<String> description = new ArrayList<String>();
		description.add(StatCollector
				.translateToLocal("extracells.tooltip.direction."
						+ this.direction.ordinal()));

		if (this.tank == null || this.tank.getFluid() == null) {
			description.add(StatCollector
					.translateToLocal("extracells.tooltip.empty1"));
		} else {
			if (this.tank.getFluid().amount > 0
					&& this.tank.getFluid().getFluid() != null) {
				String amountToText = this.tank.getFluid().amount + "mB";

				description.add(this.tank.getFluid().getFluid()
						.getLocalizedName(this.tank.getFluid()));
				description.add(amountToText);
			}
		}
		drawHoveringText(description, x, y,
				Minecraft.getMinecraft().fontRenderer);
	}

	public void drawTooltip(int x, int y) {

		List<String> description = new ArrayList<String>();

		if (this.tank == null || this.tank.getFluid() == null) {
			description.add(StatCollector
					.translateToLocal("extracells.tooltip.empty1"));
		} else {
			if (this.tank.getFluid().amount > 0
					&& this.tank.getFluid().getFluid() != null) {
				String amountToText = this.tank.getFluid().amount + "mB";

				description.add(this.tank.getFluid().getFluid()
						.getLocalizedName(this.tank.getFluid()));
				description.add(amountToText);
			}
		}
		drawHoveringText(description, x, y,
				Minecraft.getMinecraft().fontRenderer);
	}
}
