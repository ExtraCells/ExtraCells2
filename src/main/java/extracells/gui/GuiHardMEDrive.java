package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.BlockEnum;
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
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/hardmedrive.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j)
	{
		this.fontRenderer.drawString(BlockEnum.BLASTRESISTANTMEDRIVE.getStatName(), 5, 5, 0x000000);
	}

	public int getRowLength()
	{
		return 1;
	}
}
