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

public class WidgetRedstoneSwitch extends GuiButton
{
	public static RedstoneModeInput redstoneMode;

	public WidgetRedstoneSwitch(int ID, int xPos, int yPos, int width, int heigth, RedstoneModeInput mode)
	{
		super(ID, xPos, yPos, width, heigth, "ScrewStrings :D");
		redstoneMode = mode;
	}

	@Override
	public void drawButton(Minecraft minecraftInstance, int x, int y)
	{
		if (this.drawButton)
		{
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(minecraftInstance, x, y);

			// Draw redstoneMode Icon
			minecraftInstance.getTextureManager().bindTexture(new ResourceLocation("extracells", "textures/gui/redstonemodes.png"));
			drawTexturedModalRect(xPosition, yPosition, 0, 16, 16, 16);

			switch (redstoneMode)
			{
			case WhenOn:
				drawTexturedModalRect(xPosition, yPosition, 16, 0, 16, 16);
				break;
			case WhenOff:
				drawTexturedModalRect(xPosition, yPosition, 0, 0, 16, 16);
				break;
			case OnPulse:
				drawTexturedModalRect(xPosition, yPosition, 32, 0, 16, 16);
				break;
			case Ignore:
				drawTexturedModalRect(xPosition, yPosition, 48, 0, 16, 16);
				break;
			default:
				break;
			}
		}
	}

	public void setRedstoneMode(RedstoneModeInput mode)
	{
		redstoneMode = mode;
	}
}
