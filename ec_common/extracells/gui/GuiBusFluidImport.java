package extracells.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerBusFluidImport;

@SideOnly(Side.CLIENT)
public class GuiBusFluidImport extends GuiContainer
{

	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusFluidImport(IInventory inventory, IInventory tileEntity)
	{
		super(new ContainerBusFluidImport(inventory, tileEntity));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/gui/importbusfluid.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		this.fontRenderer.drawString(StatCollector.translateToLocal("tile.block.fluid.bus.import"), 5, 0, 0x000000);
	}
}
