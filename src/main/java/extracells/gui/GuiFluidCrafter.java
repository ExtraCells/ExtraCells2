package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import extracells.container.ContainerFluidCrafter;
import extracells.registries.BlockEnum;

public class GuiFluidCrafter extends GuiContainer
{
	public static final int xSize = 176;
	public static final int ySize = 166;
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/fluidcrafter.png");

	public GuiFluidCrafter(InventoryPlayer player, IInventory tileentity)
	{
		super(new ContainerFluidCrafter(player, tileentity));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int i, int j)
	{
		this.fontRendererObj.drawString(BlockEnum.FLUIDCRAFTER.getStatName(), 5, 5, 0x000000);
	}

	public int getRowLength()
	{
		return 3;
	}

}