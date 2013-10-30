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
import extracells.gui.widget.WidgetRedstoneSwitch;
import extracells.network.PacketHandler;
import extracells.network.packet.PacketBusFluidImport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusSupplier;

@SideOnly(Side.CLIENT)
public class GuiBusSupplier extends GuiContainer
{
	World world;
	EntityPlayer player;
	TileEntityBusSupplier tileentity;
	public static final int xSize = 176;
	public static final int ySize = 177;

	public GuiBusSupplier(World world, IInventory inventory, TileEntityBusSupplier TileEntity, EntityPlayer player)
	{
		super(new ContainerBusFluidImport(inventory, TileEntity.getInventory()));
		this.world = world;
		this.tileentity = TileEntity;
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
		super.drawGuiContainerForegroundLayer(sizeX, sizeY);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/importbusfluid.png"));
		this.fontRenderer.drawString(BlockEnum.SUPPLYBUS.getLocalizedName(), 5, 0, 0x000000);
	}
}
