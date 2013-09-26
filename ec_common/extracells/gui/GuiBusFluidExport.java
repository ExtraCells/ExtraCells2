package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import appeng.api.WorldCoord;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerBusFluidExport;
import extracells.network.PacketHandler;
import extracells.tile.TileEntityBusFluidExport;

@SideOnly(Side.CLIENT)
public class GuiBusFluidExport extends GuiContainer
{

	WorldCoord coord;
	World world;
	EntityPlayer player;
	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusFluidExport(IInventory inventory, IInventory tileEntity, World world, WorldCoord coord, EntityPlayer player)
	{
		super(new ContainerBusFluidExport(inventory, tileEntity));
		this.world = world;
		this.coord = coord;
		this.player = player;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/exportbusfluid.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		PacketHandler.sendFluidExportBusPacket(coord.x, coord.y, coord.z, 0, player.username);

		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/exportbusfluid.png"));
		if (world.getBlockTileEntity(coord.x, coord.y, coord.z) instanceof TileEntityBusFluidExport)
		{
			TileEntityBusFluidExport exportbus = (TileEntityBusFluidExport) world.getBlockTileEntity(coord.x, coord.y, coord.z);

			if (exportbus.getRedstoneAction())
			{
				this.drawTexturedModalRect(153, 2, 176, 16, 16, 16);
			} else
			{
				this.drawTexturedModalRect(153, 2, 176, 0, 16, 16);
			}
		}

		this.fontRenderer.drawString(StatCollector.translateToLocal("tile.block.fluid.bus.export"), 5, 0, 0x000000);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton)
	{
		super.mouseClicked(x, y, mouseButton);

		int posX = x - ((width - xSize) / 2);
		int posY = y - ((height - ySize) / 2);

		if (posX >= 153 && posX <= 169 && posY >= 2 && posY <= 18 && mouseButton == 0)
		{
			PacketHandler.sendFluidExportBusPacket(coord.x, coord.y, coord.z, 1, player.username);
		}
	}
}
