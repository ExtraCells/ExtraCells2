package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import appeng.api.WorldCoord;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.BlockNames;
import extracells.network.PacketHandler;
import extracells.network.packet.PacketMEBattery;
import extracells.tile.TileEntityMEBattery;

public class GuiMEBattery extends GuiScreen
{
	WorldCoord coord;
	World world;
	EntityPlayer player;
	private final int xSize = 176;
	private final int ySize = 88;
	private double currentEnergy;
	private double maxEnergy;

	public GuiMEBattery(World world, WorldCoord coord, EntityPlayer player)
	{
		super();
		this.world = world;
		this.coord = coord;
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

		fontRenderer.drawString(BlockNames.MEBATTERY.getLocalizedName(), posX + 5, posY + 5, 0x000000);
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
		PacketDispatcher.sendPacketToServer(new PacketMEBattery(coord.x, coord.y, coord.z, player.username).makePacket());

		if (world.getBlockTileEntity(coord.x, coord.y, coord.z) instanceof TileEntityMEBattery)
		{
			TileEntityMEBattery battery = (TileEntityMEBattery) world.getBlockTileEntity(coord.x, coord.y, coord.z);
			currentEnergy = battery.getEnergy();
			maxEnergy = battery.getMaxEnergy();
		}
	}

	@Override
	public boolean doesGuiPauseGame()
	{
		return false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;
	}
}
