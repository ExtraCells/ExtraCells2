package extracells.gui.widget;

import appeng.api.storage.data.IAEFluidStack;
import extracells.gui.GuiFluidTerminal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraftforge.fluids.Fluid;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WidgetFluidSelector extends AbstractFluidWidget
{
	private long amount = 0;
	private int color;
	private int borderThickness;

	public WidgetFluidSelector(GuiFluidTerminal guiFluidTerminal, IAEFluidStack stack)
	{
		super(guiFluidTerminal, 18, 18, stack.getFluidStack().getFluid());
		amount = stack.getStackSize();
		color = 0xFF00FFFF;
		borderThickness = 1;
	}

	@Override
	public void drawWidget(int posX, int posY)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		Fluid currentFluid = guiFluidTerminal.currentFluid != null ? guiFluidTerminal.currentFluid.getFluid() : null;
		if (fluid != null && fluid.getIcon() != null)
			drawTexturedModelRectFromIcon(posX + 1, posY + 1, fluid.getIcon(), height - 2, width - 2);
		if (fluid == currentFluid)
			drawHollowRectWithCorners(posX, posY, height, width, color, borderThickness);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public void drawTooltip(int posX, int posY, int mouseX, int mouseY)
	{
		if (fluid != null && isPointInRegion(posX, posY, height, width, mouseX, mouseY))
		{
			if (amount > 0 && fluid != null)
			{
				String amountToText = Long.toString(amount) + "mB";
				if (true)// Extracells.shortenedBuckets)
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
				drawHoveringText(description, mouseX - guiFluidTerminal.guiLeft(), mouseY - guiFluidTerminal.guiTop(), Minecraft.getMinecraft().fontRenderer);
			}
		}
	}

	@Override
	public void mouseClicked(int posX, int posY, int mouseX, int mouseY)
	{
		if (fluid != null && isPointInRegion(posX, posY, height, width, mouseX, mouseY))
		{
			guiFluidTerminal.containerTerminalFluid().setSelectedFluid(fluid);
		}
	}

	public void setAmount(long amount)
	{
		this.amount = amount;
	}

	public long getAmount()
	{
		return amount;
	}

	private void drawHollowRectWithCorners(int posX, int posY, int heigth, int width, int color, int thickness)
	{
		drawRect(posX, posY, posX + heigth, posY + thickness, color);
		drawRect(posX, posY + width - thickness, posX + heigth, posY + width, color);
		drawRect(posX, posY, posX + thickness, posY + width, color);
		drawRect(posX + heigth - thickness, posY, posX + heigth, posY + width, color);

		drawRect(posX, posY, posX + thickness + 1, posY + thickness + 1, color);
		drawRect(posX + heigth, posY + width, posX + heigth - thickness - 1, posY + width - thickness - 1, color);
		drawRect(posX + heigth, posY, posX + heigth - thickness - 1, posY + thickness + 1, color);
		drawRect(posX, posY + width, posX + thickness + 1, posY + width - thickness - 1, color);
	}
}
