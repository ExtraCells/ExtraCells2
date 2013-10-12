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
import extracells.container.ContainerBusFluidExport;
import extracells.gui.widget.WidgetRedstoneSwitch;
import extracells.network.packet.PacketBusFluidExport;
import extracells.tile.TileEntityBusFluidExport;

@SideOnly(Side.CLIENT)
public class GuiBusFluidExport extends GuiContainer
{
	World world;
	EntityPlayer player;
	TileEntityBusFluidExport tileentity;
	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusFluidExport(World world, IInventory inventory, TileEntityBusFluidExport tileentity, EntityPlayer player)
	{
		super(new ContainerBusFluidExport(inventory, tileentity.getInventory()));
		this.world = world;
		this.tileentity = tileentity;
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
		PacketDispatcher.sendPacketToServer(new PacketBusFluidExport(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, 0, player.username).makePacket());

		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/exportbusfluid.png"));

		if (tileentity instanceof TileEntityBusFluidExport)
		{
			WidgetRedstoneSwitch button = (WidgetRedstoneSwitch) buttonList.get(0);
			button.setRedstoneMode(tileentity.getRedstoneAction());
		}

		this.fontRenderer.drawString(BlockEnum.FLUIDEXPORT.getLocalizedName(), 5, 0, 0x000000);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		buttonList.add(new WidgetRedstoneSwitch(0, guiLeft + 153, guiTop + 2, 16, 16, tileentity.getRedstoneAction()));
	}

	public void actionPerformed(GuiButton button)
	{
		int modeOrdinal = tileentity.getRedstoneAction().ordinal();
		switch (button.id)
		{
		case 0:
			PacketDispatcher.sendPacketToServer(new PacketBusFluidExport(world, tileentity.xCoord, tileentity.yCoord, tileentity.zCoord, 1, player.username).makePacket());
			break;
		default:
		}
	}
}
