package extracells.gui.widget;

import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.Extracells;
import extracells.gui.GuiTerminalFluid;
import extracells.network.packet.PacketTerminalFluid;
import extracells.tileentity.TileEntityTerminalFluid;
import extracells.util.SpecialFluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class WidgetFluidSelector extends AbstractFluidWidget
{
	private long amount = 0;
	private int color;
	private int borderThickness;
	private boolean selected = false;

	public WidgetFluidSelector(GuiTerminalFluid guiTerminalFluid, SpecialFluidStack stack)
	{
		super(guiTerminalFluid, 18, 18, stack.getFluidStack().getFluid());
		amount = stack.getAmount();
		color = 0xFF00FFFF;
		borderThickness = 1;
	}

	@Override
	public void drawWidget(int posX, int posY)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		if (fluid != null && fluid.getIcon() != null)
			drawTexturedModelRectFromIcon(posX + 1, posY + 1, fluid.getIcon(), sizeX - 2, sizeY - 2);
		if (selected)
			drawHollowRectWithCorners(posX, posY, sizeX, sizeY, color, borderThickness);
		GL11.glEnable(GL11.GL_LIGHTING);
	}

	@Override
	public void drawTooltip(int posX, int posY, int mouseX, int mouseY)
	{
		if (fluid != null && isPointInRegion(posX, posY, sizeX, sizeY, mouseX, mouseY))
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
				drawHoveringText(description, mouseX - guiTerminalFluid.guiLeft(), mouseY - guiTerminalFluid.guiTop(), Minecraft.getMinecraft().fontRenderer);
			}
		}
	}

	@Override
	public void mouseClicked(int posX, int posY, int mouseX, int mouseY)
	{
		if (fluid != null && isPointInRegion(posX, posY, sizeX, sizeY, mouseX, mouseY))
		{
			TileEntityTerminalFluid terminalFluid = guiTerminalFluid.tileEntity;
			PacketDispatcher.sendPacketToServer(new PacketTerminalFluid(terminalFluid.worldObj, terminalFluid.xCoord, terminalFluid.yCoord, terminalFluid.zCoord, fluid).makePacket());
			selected = true;
			guiTerminalFluid.updateSelected(this);
			guiTerminalFluid.currentFluidAmount = amount;
			guiTerminalFluid.currentFluidName = fluid.getLocalizedName();
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

	public void setSelected(Boolean selected)
	{
		this.selected = selected;
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
}
