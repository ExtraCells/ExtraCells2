package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.BlockEnum;
import extracells.network.packet.PacketMEBattery;
import extracells.tile.TileEntityMEBattery;

public class GuiMEBattery extends GuiScreen
{
	TileEntityMEBattery tileEntity;
	World world;
	EntityPlayer player;
	private final int xSize = 176;
	private final int ySize = 88;
	private double currentEnergy;
	private double maxEnergy;

	public GuiMEBattery(World world, TileEntityMEBattery tileEntity, EntityPlayer player)
	{
		super();
		this.world = world;
		this.tileEntity = tileEntity;
		this.player = player;

	}

	@Override
	public void drawScreen(int x, int y, float f)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/solderingstation.png"));
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);

		fontRenderer.drawString(BlockEnum.MEBATTERY.getStatName(), posX + 5, posY + 5, 0x000000);
		fontRenderer.drawString("Energy: " + currentEnergy + "/" + maxEnergy, posX + 5, posY + 15, 0x000000);

		super.drawScreen(x, y, f);
	}

	public void keyTyped(char key, int par2)
	{
		if (key == 'e' || key == '')
		{
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	public void updateScreen()
	{
		PacketDispatcher.sendPacketToServer(new PacketMEBattery(world, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, player.username).makePacket());

		if (world.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord) instanceof TileEntityMEBattery)
		{
			TileEntityMEBattery battery = (TileEntityMEBattery) world.getBlockTileEntity(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord);
			currentEnergy = battery.getEnergy();
			maxEnergy = battery.getMaxEnergy();
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}
}
