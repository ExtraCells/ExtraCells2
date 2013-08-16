package extracells.gui;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerHardMEDrive;
import extracells.tile.TileEntityHardMEDrive;

@SideOnly(Side.CLIENT)
public class GuiHardMEDrive extends GuiContainer
{

	public static final int xSize = 176;
	public static final int ySize = 166;

	public GuiHardMEDrive(InventoryPlayer inventory, TileEntityHardMEDrive tileentity)
	{
		super(new ContainerHardMEDrive(inventory, tileentity));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/gui/hardmedrive.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j)
	{
		this.fontRenderer.drawString(StatCollector.translateToLocal("tile.block.hardmedrive"), 0, 0, 0x000000);
	}

	public int getRowLength()
	{
		return 1;
	}
}
