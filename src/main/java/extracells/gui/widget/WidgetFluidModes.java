package extracells.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class WidgetFluidModes extends GuiButton
{
	private FluidMode fluidMode;

	public WidgetFluidModes(int ID, int xPos, int yPos, int width, int heigth, FluidMode mode)
	{
		super(ID, xPos, yPos, width, heigth, "ScrewStrings :D");
		fluidMode = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y)
	{
		if (drawButton)
		{
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(minecraftInstance, x, y);

			minecraftInstance.getTextureManager().bindTexture(new ResourceLocation("extracells", "textures/gui/fluidmodes.png"));
			drawTexturedModalRect(xPosition, yPosition, 0, 16, 16, 16);

			switch (fluidMode)
			{
			case DROPS:
				drawTexturedModalRect(xPosition, yPosition, 0, 0, 16, 16);
				break;
			case QUART:
				drawTexturedModalRect(xPosition, yPosition, 16, 0, 16, 16);
				break;
			case BUCKETS:
				drawTexturedModalRect(xPosition, yPosition, 32, 0, 16, 16);
				break;
			default:
				break;
			}

			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution scaledresolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

			int mouseX = Mouse.getX() * scaledresolution.getScaledWidth() / mc.displayWidth;
			int mouseY = scaledresolution.getScaledHeight() - Mouse.getY() * scaledresolution.getScaledHeight() / mc.displayHeight - 1;

			if (mouseX >= xPosition && mouseX <= xPosition + width && mouseY >= yPosition && mouseY <= yPosition + height)
			{
				List<String> description = new ArrayList<String>();
				description.add(StatCollector.translateToLocal("tooltip.fluidmode"));
				description.add(StatCollector.translateToLocal("tooltip.fluidmode.move").replace("$amount", Integer.toString(fluidMode.getAmount())));
				description.add(StatCollector.translateToLocal("tooltip.fluidmode.cost").replace("$cost", Float.toString(fluidMode.getCost())));
				drawHoveringText(description, mouseX, mouseY, mc.fontRenderer);
			}
		}
	}

	public void setFluidMode(FluidMode mode)
	{
		fluidMode = mode;
	}

	public FluidMode getFluidMode()
	{
		return fluidMode;
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

	public enum FluidMode
	{

		DROPS(20, 5F),
		QUART(250, 30F),
		BUCKETS(1000, 60F);

		private int amount;
		private float cost;

		FluidMode(int amount, float cost)
		{
			this.amount = amount;
			this.cost = cost;
		}

		public int getAmount()
		{
			return amount;
		}

		public float getCost()
		{
			return cost;
		}
	}

}
