package extracells.gui.widget;

import extracells.Extracells;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WidgetFluidSelector extends Gui
{
	public int posX = 0, posY = 0;
	public int sizeX = 0, sizeY = 0;
	Fluid fluid;
	long amount = 0;
	int color = 0xFF00FFFF; // Full Brightness Cyan
	int borderThickness = 1; // Two pixel thick border if selected;
	boolean selected = false;

	public WidgetFluidSelector(int posX, int posY, int sizeX, int sizeY, Fluid fluid, long amount, int color, int borderThickness)
	{
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.fluid = fluid;
		this.amount = amount;
		this.color = color;
		this.borderThickness = borderThickness;
	}

	public WidgetFluidSelector(int posX, int posY, Fluid fluid, long amount, int color, int borderThickness)
	{
		this(posX, posY, 18, 18, fluid, amount, color, borderThickness);
	}

	public void drawSelector(Minecraft minecraftInstance, int x, int y)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		if (fluid != null && fluid.getIcon() != null)
			drawTexturedModelRectFromIcon(x + posX + 1, y + posY + 1, fluid.getIcon(), sizeX - 2, sizeY - 2);
		if (selected)
			drawHollowRectWithCorners(x + posX, y + posY, sizeX, sizeY, color, borderThickness);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void drawTooltip(int x, int y)
	{
		if (amount > 0 && fluid != null)
		{
			String amountToText = Long.toString(amount) + "mB";
			if (Extracells.shortenedBuckets)
			{
				if (amount > 1000000000L)
					amountToText = Long.toString(amount / 1000000000L) + "MegaB";
				else if (amount > 1000000L)
					amountToText = Long.toString(amount / 1000000L) + "KiloB";
				else if (amount > 9999L)
				{
					amountToText = Long.toString(amount / 1000L) + "B";
				}
			}

			List<String> description = new ArrayList<String>();
			description.add(fluid.getLocalizedName());
			description.add(amountToText);
			drawHoveringText(description, x, y, Minecraft.getMinecraft().fontRenderer);
		}
	}

	public void setFluid(Fluid fluid)
	{
		this.fluid = fluid;
	}

	public void setFluid(int fluidID)
	{
		this.fluid = FluidRegistry.getFluid(fluidID);
	}

	public Fluid getFluid()
	{
		return fluid;
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public long getAmount()
	{
		return amount;
	}

	public void setSelected(Boolean selected)
	{
		this.selected = selected;
	}

	public boolean isSelected()
	{
		return selected;
	}

	private void drawHollowRectWithCorners(int posX, int posY, int sizeX, int sizeY, int color, int thickness)
	{
		drawRect(posX, posY, posX + sizeX, posY + thickness, color);
		drawRect(posX, posY + sizeY - thickness, posX + sizeX, posY + sizeY, color);
		drawRect(posX, posY, posX + thickness, posY + sizeY, color);
		drawRect(posX + sizeX - thickness, posY, posX + sizeX, posY + sizeY, color);

		drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color);
		drawRect(posX + sizeX, posY + sizeY, posX + sizeX - thickness - 1, posY + sizeY - thickness - 1, color);
		drawRect(posX + sizeX, posY, posX + sizeX - thickness - 1, posY + thickness + 1, color);
		drawRect(posX, posY + sizeY, posX + thickness + 1, posY + sizeY - thickness - 1, color);
	}

	@SuppressWarnings("rawtypes")
	protected void drawHoveringText(List list, int x, int y, FontRenderer fontrenderer)
	{
		if (!list.isEmpty())
		{
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;
			Iterator iterator = list.iterator();

			while (iterator.hasNext())
			{
				String s = (String) iterator.next();
				int l = fontrenderer.getStringWidth(s);

				if (l > k)
				{
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if (list.size() > 1)
			{
				k1 += 2 + (list.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < list.size(); ++k2)
			{
				String s1 = (String) list.get(k2);
				fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0)
				{
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
}
