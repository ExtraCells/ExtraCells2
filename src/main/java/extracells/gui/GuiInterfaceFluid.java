package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import extracells.BlockEnum;
import extracells.container.ContainerInterfaceFluid;
import extracells.gui.widget.WidgetFluidTank;
import extracells.tileentity.TileEntityInterfaceFluid;

public class GuiInterfaceFluid extends GuiContainer
{
	TileEntityInterfaceFluid tileentity;
	WidgetFluidTank[] tanks = new WidgetFluidTank[6];
	public static final int xSize = 176;
	public static final int ySize = 196;

	public GuiInterfaceFluid(IInventory player, TileEntityInterfaceFluid tileentity)
	{
		super(new ContainerInterfaceFluid(player, tileentity.getInventory()));
		this.tileentity = tileentity;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		for (int i = 0; i < tanks.length; i++)
		{
			tanks[i] = new WidgetFluidTank(tileentity.tanks[i], i * 20 + 29, 7, ForgeDirection.getOrientation(i));
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/interfacefluid.png"));
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRenderer.drawString(BlockEnum.FLUIDINTERFACE.getStatName(), 5, 103, 0x000000);
		for (WidgetFluidTank tank : tanks)
		{
			tank.draw(guiLeft, guiTop, mouseX, mouseY);
		}
		for (WidgetFluidTank tank : tanks)
		{
			if (isPointInRegion(tank.posX, tank.posY, 18, 73, mouseX, mouseY))
			{
				tank.drawTooltip(mouseX - guiLeft, mouseY - guiTop);
			}
		}
	}

}
