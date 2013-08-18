package extracells.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.opengl.GL11;

import appeng.api.WorldCoord;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerTerminalFluid;
import extracells.network.PacketHandler;
import extracells.tile.TileEntityTerminalFluid;

@SideOnly(Side.CLIENT)
public class GuiTerminalFluid extends GuiContainer
{

	public static final int xSize = 176;
	public static final int ySize = 175;
	public String fluidName;
	public WorldCoord tilePos;
	TileEntityTerminalFluid tileEntity;
	ContainerTerminalFluid container;

	public GuiTerminalFluid(int x, int y, int z, InventoryPlayer inventory, TileEntity tileEntity)
	{
		super(new ContainerTerminalFluid(inventory, tileEntity));
		container = (ContainerTerminalFluid) this.inventorySlots;
		this.tileEntity = (TileEntityTerminalFluid) tileEntity;
		tilePos = new WorldCoord(x, y, z);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		super.drawScreen(mouseX, mouseY, f);
	}

	@Override
	public void initGui()
	{
		super.initGui();
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		this.buttonList.clear();

		// Up
		this.buttonList.add(new GuiButton(0, posX + 155, posY + 18, 10, 10, "\u25B2"));
		// Down
		this.buttonList.add(new GuiButton(1, posX + 155, posY + 29, 10, 10, "\u25BC"));
	}

	public void actionPerformed(GuiButton button)
	{
		switch (button.id)
		{
		case 0:
			PacketHandler.sendMonitorFluidPacket(tilePos.x, tilePos.y, tilePos.z, 0);
			break;
		case 1:
			PacketHandler.sendMonitorFluidPacket(tilePos.x, tilePos.y, tilePos.z, 1);
			break;
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/gui/terminalfluid.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		this.fontRenderer.drawString(StatCollector.translateToLocal("tile.block.fluid.terminal"), 5, 0, 0x000000);

		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		long amount;
		String fluidname;

		if (container.getSlot(2).getStack() != null && container.getSlot(2).getStack().getTagCompound() != null)
		{
			amount = container.getSlot(2).getStack().getTagCompound().getLong("amount");
			fluidname = container.getSlot(2).getStack().getTagCompound().getString("fluidname");
		} else
		{
			amount = 0;
			fluidname = "---";
		}

		String suffix = "mB";

		if (amount > 100000000)
		{
			amount = amount / 1000000;
			suffix = "M mB";
		} else if (amount > 100000)
		{
			amount = amount / 1000;
			suffix = "K mB";
		}

		this.fontRenderer.drawString(StatCollector.translateToLocal("tooltip.amount") + ": " + amount + suffix, 13, 15, 0xFFFFFF);
		this.fontRenderer.drawString(StatCollector.translateToLocal("tooltip.fluid") + ": " + fluidname, 13, 25, 0xFFFFFF);
	}

	public int getRowLength()
	{
		return 2;
	}
}
