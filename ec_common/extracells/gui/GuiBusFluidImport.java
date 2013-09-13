package extracells.gui;

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
import extracells.container.ContainerBusFluidImport;
import extracells.network.PacketHandler;
import extracells.tile.TileEntityBusFluidImport;

@SideOnly(Side.CLIENT)
public class GuiBusFluidImport extends GuiContainer
{
	WorldCoord coord;
	World world;
	EntityPlayer player;
	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusFluidImport(IInventory inventory, IInventory tileEntity, World world, WorldCoord coord, EntityPlayer player)
	{
		super(new ContainerBusFluidImport(inventory, tileEntity));
		this.world = world;
		this.coord = coord;
		this.player = player;
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
		PacketHandler.sendFluidImportBusPacket(coord.x, coord.y, coord.z, 0, player.username);

		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/gui/importbusfluid.png"));

		if (world.getBlockTileEntity(coord.x, coord.y, coord.z) instanceof TileEntityBusFluidImport)
		{
			TileEntityBusFluidImport importbus = (TileEntityBusFluidImport) world.getBlockTileEntity(coord.x, coord.y, coord.z);

			if (importbus.getRedstoneAction())
			{
				this.drawTexturedModalRect(153, 2, 176, 16, 16, 16);
			} else
			{
				this.drawTexturedModalRect(153, 2, 176, 0, 16, 16);
			}
		}

		this.fontRenderer.drawString(StatCollector.translateToLocal("tile.block.fluid.bus.import"), 5, 0, 0x000000);
	}

	@Override
	protected void mouseClicked(int x, int y, int mouseButton)
	{
		super.mouseClicked(x, y, mouseButton);

		int posX = x - ((width - xSize) / 2);
		int posY = y - ((height - ySize) / 2);

		if (posX >= 153 && posX <= 169 && posY >= 2 && posY <= 18 && mouseButton == 0)
		{
			PacketHandler.sendFluidImportBusPacket(coord.x, coord.y, coord.z, 1, player.username);
		}
	}
}
