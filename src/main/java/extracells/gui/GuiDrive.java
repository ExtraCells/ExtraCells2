package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

import extracells.container.ContainerDrive;
import extracells.network.packet.part.PacketFluidStorage;
import extracells.part.PartDrive;

public class GuiDrive extends GuiContainer
{
	private EntityPlayer player;
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/drive.png");

	public GuiDrive(PartDrive _part, EntityPlayer _player)
	{
		super(new ContainerDrive(_part, _player));// TODO
		player = _player;
		xSize = 176;
		ySize = 163;
		new PacketFluidStorage(player).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(guiLeft, guiTop - 18, 0, 0, xSize, ySize);
	}
}
