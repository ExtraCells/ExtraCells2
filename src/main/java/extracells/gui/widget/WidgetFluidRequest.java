package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.List;

public class WidgetFluidRequest extends Gui
{
	public int posX = 0, posY = 0;
	public int sizeX = 0, sizeY = 0;
	Fluid fluid;

	public WidgetFluidRequest(int posX, int posY, int sizeX, int sizeY, Fluid fluid)
	{
		this.posX = posX;
		this.posY = posY;
		this.sizeX = sizeX;
		this.sizeY = sizeY;
		this.fluid = fluid;
	}

	public void drawRequester(Minecraft minecraftInstance, int x, int y)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		if (fluid != null && fluid.getIcon() != null)
		{
			drawTexturedModelRectFromIcon(x + posX + 1, y + posY + 1, fluid.getIcon(), sizeX - 2, sizeY - 2);
			minecraftInstance.fontRenderer.drawString(StatCollector.translateToLocal("AppEng.Terminal.Craft"), 0, 0, 0);
		}
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	public void drawTooltip(int x, int y)
	{
		if (fluid != null)
		{
			List<String> description = new ArrayList<String>();
			description.add(StatCollector.translateToLocal("AppEng.GuiITooltip.Craftable"));
			description.add(fluid.getLocalizedName());
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

			for (Object string : list)
			{
				String s = (String) string;
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
