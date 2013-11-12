package extracells.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.BlockEnum;
import extracells.SpecialFluidStack;
import extracells.container.ContainerTerminalFluid;
import extracells.gui.widget.WidgetFluidSelector;
import extracells.network.packet.PacketMonitorFluid;
import extracells.tile.TileEntityTerminalFluid;

@SideOnly(Side.CLIENT)
public class GuiTerminalFluid extends GuiContainer
{

	public static final int xSize = 175;
	public static final int ySize = 203;
	private int currentScroll = 0;
	public String fluidName;
	TileEntityTerminalFluid tileEntity;
	List<WidgetFluidSelector> selectors = new ArrayList<WidgetFluidSelector>();

	public GuiTerminalFluid(World world, TileEntityTerminalFluid tileEntity, InventoryPlayer inventory)
	{
		super(new ContainerTerminalFluid(inventory, tileEntity.getInventory()));
		this.tileEntity = tileEntity;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float f)
	{
		super.drawScreen(mouseX, mouseY, f);
		int fluidTypes = 0;
		if (tileEntity != null && !tileEntity.getFluids().isEmpty())
		{
			List<SpecialFluidStack> fluidList = tileEntity.getFluids();
			fluidTypes = fluidList.size();
			for (int i = currentScroll * 9; i < fluidList.size(); i++)
			{
				selectors.get(i - currentScroll * 9).setFluid(fluidList.get(i).getFluid());
				selectors.get(i - currentScroll * 9).setAmount(fluidList.get(i).amount);
			}
			for (int i = fluidList.size() - currentScroll * 9; i >= 0 && i < selectors.size(); i++)
			{
				selectors.get(i).setFluid(null);
				selectors.get(i).setAmount(-1);
			}

			Fluid currentFluid = tileEntity.getCurrentFluid();
			for (WidgetFluidSelector selector : selectors)
			{
				selector.setSelected(currentFluid != null ? selector.getFluid() == currentFluid : false);
			}
		} else
		{
			fluidTypes = 0;
			for (WidgetFluidSelector selector : selectors)
			{
				selector.setFluid(null);
				selector.setAmount(-1);
			}

			for (WidgetFluidSelector selector : selectors)
			{
				selector.setSelected(false);
			}
		}
		int deltaWheel = Mouse.getDWheel();

		if (deltaWheel > 0)
		{
			currentScroll--;
		} else if (deltaWheel < 0)
		{
			currentScroll++;
		}

		if (currentScroll < 0)
			currentScroll = 0;
		if (fluidTypes / 9 < 4 && currentScroll < fluidTypes / 9 + 4)
			currentScroll = 0;
	}

	@Override
	public void initGui()
	{
		Mouse.getDWheel();
		super.initGui();
		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		for (int y = 0; y < 4 * 18; y += 18)
		{
			for (int x = 0; x < 9 * 18; x += 18)
			{
				selectors.add(new WidgetFluidSelector(x + 7, y - 1, null, 0, 0XFF00FFFF, 1));
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/gui/terminalfluid.png"));
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int sizeX, int sizeY)
	{
		this.fontRenderer.drawString(BlockEnum.FLUIDTERMINAL.getLocalizedName(), 5, -12, 0x000000);

		int posX = (this.width - xSize) / 2;
		int posY = (this.height - ySize) / 2;

		long amount = 0;
		String name = "";
		for (WidgetFluidSelector selector : selectors)
		{
			selector.drawSelector();
			if (selector.isSelected())
			{
				amount = selector.getAmount();
				name = selector.getFluid().getLocalizedName();
			}
		}

		String amountToText = amount + "mB";

		if (amount > (long) Math.pow(10, 6))
		{
			amountToText = amount / Math.pow(10, 3) + "B";
		} else if (amount > (long) Math.pow(10, 9))
		{
			amountToText = amount / Math.pow(10, 6) + "kB";
		} else if (amount > (long) Math.pow(10, 12))
		{
			amountToText = amount / Math.pow(10, 9) + "MB";
		} else if (amount > (long) Math.pow(10, 15))
		{
			amountToText = amount / Math.pow(10, 12) + "GB";
		} else if (amount > (long) Math.pow(10, 18))
		{
			amountToText = amount / Math.pow(10, 15) + "TB";
		}

		fontRenderer.drawString(StatCollector.translateToLocal("tooltip.amount") + ": " + amountToText, 45, 73, 0x000000);
		fontRenderer.drawString(StatCollector.translateToLocal("tooltip.fluid") + ": " + name, 45, 83, 0x000000);

		int mouseX = Mouse.getX() * this.width / this.mc.displayWidth;
		int mouseY = this.height - Mouse.getY() * this.height / this.mc.displayHeight - 1;

		for (WidgetFluidSelector selector : selectors)
		{
			if (isPointInRegion(selector.posX, selector.posY, selector.sizeX, selector.sizeY, mouseX, mouseY))
			{
				selector.drawTooltip(mouseX - posX, mouseY - posY);
			}
		}
	}

	protected void mouseClicked(int x, int y, int mouseBtn)
	{
		super.mouseClicked(x, y, mouseBtn);
		for (WidgetFluidSelector selector : selectors)
		{
			if (selector.getFluid() != null)
			{
				if (isPointInRegion(selector.posX, selector.posY, selector.sizeX, selector.sizeY, x, y))
				{
					PacketDispatcher.sendPacketToServer(new PacketMonitorFluid(tileEntity.worldObj, tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, selector.getFluid().getID()).makePacket());
				}
			}
		}
	}
}
