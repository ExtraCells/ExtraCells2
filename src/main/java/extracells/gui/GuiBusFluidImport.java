package extracells.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.BlockEnum;
import extracells.container.ContainerBusFluidImport;
import extracells.gui.widget.WidgetFluidModes;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.gui.widget.WidgetFluidModes.FluidMode;
import extracells.network.PacketHandler;
import extracells.network.packet.PacketBusFluidExport;
import extracells.network.packet.PacketBusFluidImport;
import extracells.tile.TileEntityBusFluidImport;

@SideOnly(Side.CLIENT)
public class GuiBusFluidImport extends GuiContainer
{
	World world;
	EntityPlayer player;
	TileEntityBusFluidImport tileentity;
	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusFluidImport(World world, IInventory inventory, TileEntityBusFluidImport tileentity, EntityPlayer player)
	{
		super(new ContainerBusFluidImport(inventory, tileentity.getInventory()));
		this.world = world;
		this.tileentity = tileentity;
		this.player = player;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/importbusfluid.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		PacketDispatcher.sendPacketToServer(new PacketBusFluidImport(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, 0, player.username).makePacket());
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/importbusfluid.png"));

		if (tileentity instanceof TileEntityBusFluidImport)
		{
			WidgetRedstoneModes redstoneSwitch = (WidgetRedstoneModes) buttonList.get(0);
			redstoneSwitch.setRedstoneMode(tileentity.getRedstoneMode());
			WidgetFluidModes fluidSwitch = (WidgetFluidModes) buttonList.get(1);
			fluidSwitch.setFluidMode(tileentity.getFluidMode());
		}

		this.fontRenderer.drawString(BlockEnum.FLUIDIMPORT.getLocalizedName(), 5, 0, 0x000000);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add(new WidgetRedstoneModes(0, guiLeft + 126, guiTop + 19, 16, 16, tileentity.getRedstoneMode()));
		buttonList.add(new WidgetFluidModes(1, guiLeft + 126, guiTop + 41, 16, 16, FluidMode.BUCKETS));
	}

	public void actionPerformed(GuiButton button)
	{
		int modeOrdinal = tileentity.getRedstoneMode().ordinal();
		switch (button.id)
		{
		case 0:
			PacketDispatcher.sendPacketToServer(new PacketBusFluidImport(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, 1, player.username).makePacket());
			break;
		case 1:
			PacketDispatcher.sendPacketToServer(new PacketBusFluidExport(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, 2, player.username).makePacket());
			break;
		default:
		}
	}
}
