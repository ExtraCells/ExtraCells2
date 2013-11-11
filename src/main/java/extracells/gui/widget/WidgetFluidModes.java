package extracells.gui.widget;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import appeng.api.config.RedstoneModeInput;

public class WidgetFluidModes extends GuiButton
{
	public static FluidMode fluidMode;

	public WidgetFluidModes(int ID, int xPos, int yPos, int width, int heigth, FluidMode mode)
	{
		super(ID, xPos, yPos, width, heigth, "ScrewStrings :D");
		fluidMode = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y)
	{
		if (this.drawButton)
		{
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(minecraftInstance, x, y);

			// Draw redstoneMode Icon
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
