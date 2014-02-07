package extracells.gui;

import appeng.api.config.RedstoneMode;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.container.ContainerBusIOFluid;
import extracells.gui.widget.WidgetFluidModes;
import extracells.gui.widget.WidgetFluidSlot;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.network.packet.PacketBusIOFluid;
import extracells.part.PartFluidIO;
import extracells.util.FluidMode;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiBusIOFluid extends GuiContainer
{
	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
	public static final int xSize = 176;
	public static final int ySize = 177;
	private PartFluidIO part;
	private EntityPlayer player;
	private List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();

	public GuiBusIOFluid(PartFluidIO _terminal, EntityPlayer _player)
	{
		super(new ContainerBusIOFluid(_terminal, _player));
		((ContainerBusIOFluid) inventorySlots).setGui(this);
		part = _terminal;
		player = _player;
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				fluidSlotList.add(new WidgetFluidSlot(part, j + i * 4, 52 + j * 18, i * 18 + 20));
			}
		}
		PacketDispatcher.sendPacketToServer(new PacketBusIOFluid(part).makePacket());
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui()
	{
		super.initGui();
		buttonList.add(new WidgetRedstoneModes(0, guiLeft + 126, guiTop + 19, 16, 16, part.getRedstoneMode()));
		buttonList.add(new WidgetFluidModes(1, guiLeft + 126, guiTop + 41, 16, 16, part.getFluidMode()));
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX, int mouseY)
	{
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		int posX = (width - xSize) / 2;
		int posY = (height - ySize) / 2;
		drawTexturedModalRect(posX, posY, 0, 0, xSize, ySize);
	}

	public void shiftClick(ItemStack itemStack)
	{
		FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
		for (byte i = 0; i < fluidSlotList.size(); i++)
		{
			WidgetFluidSlot fluidSlot = fluidSlotList.get(i);
			if (fluidSlot.getFluid() == null || (fluid != null && fluidSlot.getFluid() == fluid))
			{
				fluidSlot.mouseClicked(itemStack);
				return;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		for (WidgetFluidSlot fluidSlot : fluidSlotList)
		{
			fluidSlot.drawWidget();
		}
		for (WidgetFluidSlot fluidSlot : fluidSlotList)
		{
			if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY))
			{
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				drawGradientRect(fluidSlot.getPosX() + 1, fluidSlot.getPosY() + 1, fluidSlot.getPosX() + 17, fluidSlot.getPosY() + 17, -0x7F000001, -0x7F000001);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				break;
			}
		}
	}

	public void updateFluids(List<Fluid> fluidList)
	{
		for (int i = 0; i < fluidSlotList.size() && i < fluidList.size(); i++)
		{
			fluidSlotList.get(i).setFluid(fluidList.get(i));
		}
	}

	public void updateButtons(byte mode, byte ordinal)
	{
		try
		{
			switch (mode)
			{
			case 0:
				((WidgetRedstoneModes) buttonList.get(0)).setRedstoneMode(RedstoneMode.values()[ordinal]);
				break;
			case 1:
				((WidgetFluidModes) buttonList.get(1)).setFluidMode(FluidMode.values()[ordinal]);
				break;
			}
		} catch (Throwable e)
		{
		}
	}

	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn)
	{
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		for (WidgetFluidSlot slot : fluidSlotList)
		{
			if (isPointInRegion(slot.getPosX(), slot.getPosY(), 18, 18, mouseX, mouseY))
			{
				slot.mouseClicked(player.inventory.getItemStack());
				break;
			}
		}
	}

	public void actionPerformed(GuiButton button)
	{
		PacketDispatcher.sendPacketToServer(new PacketBusIOFluid((byte) button.id, part).makePacket());
	}
}
