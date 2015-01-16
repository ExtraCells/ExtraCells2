package extracells.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import extracells.api.IFluidInterface;
import extracells.container.ContainerFluidInterface;
import extracells.gui.widget.WidgetFluidTank;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.common.util.ForgeDirection;

public class GuiFluidInterface extends GuiContainer
{
	IFluidInterface fluidInterface;
	public WidgetFluidTank[] tanks = new WidgetFluidTank[6];
	public WidgetFluidSlot[] filter = new WidgetFluidSlot[6];
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/interfacefluid.png");
	private EntityPlayer player;
	private ForgeDirection partSide = ForgeDirection.UNKNOWN;

	public GuiFluidInterface(EntityPlayer player, IFluidInterface fluidInterface)
	{
		super(new ContainerFluidInterface(player, fluidInterface));
		ySize = 230;
		this.fluidInterface = fluidInterface;
		this.player = player;
		((ContainerFluidInterface) inventorySlots).gui = this;
	}
	
	public GuiFluidInterface(EntityPlayer player, IFluidInterface fluidInterface, ForgeDirection side){
		this(player, fluidInterface);
		partSide = side;
	}

	@Override
	public void initGui()
	{
		super.initGui();
		guiLeft = (width - xSize) / 2;
		guiTop = (height - ySize) / 2;
		for (int i = 0; i < tanks.length; i++)
		{
			if(partSide != null && partSide != ForgeDirection.UNKNOWN &&partSide.ordinal() != i)
				continue;
			tanks[i] = new WidgetFluidTank(fluidInterface.getFluidTank(ForgeDirection.getOrientation(i)), i * 20 + 30, 16, ForgeDirection.getOrientation(i));
			if(fluidInterface instanceof IFluidSlotPartOrBlock){
				filter[i] = new WidgetFluidSlot(player, (IFluidSlotPartOrBlock) fluidInterface, i, i * 20 + 30, 93);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
	{
		drawDefaultBackground();
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
	{
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fontRendererObj.drawString(Item.getItemFromBlock(BlockEnum.ECBASEBLOCK.getBlock()).getItemStackDisplayName(new ItemStack(BlockEnum.ECBASEBLOCK.getBlock(), 1, 0)).replace("ME ", ""), 8, 5, 0x000000);
		this.fontRendererObj.drawString(StatCollector.translateToLocal("container.inventory"), 8, 136, 0x000000);
		for (WidgetFluidTank tank : tanks)
		{
			if(tank != null)
				tank.draw(guiLeft, guiTop, mouseX, mouseY);
		}
		for (WidgetFluidSlot slot : filter){
			if(slot != null)
				slot.drawWidget();
		}
		for (WidgetFluidTank tank : tanks)
		{
			if(tank != null)
				if (func_146978_c(tank.posX, tank.posY, 18, 73, mouseX, mouseY))
				{
					tank.drawTooltip(mouseX - guiLeft, mouseY - guiTop);
				}
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
        super.mouseClicked(mouseX, mouseY, mouseBtn);
        for (WidgetFluidSlot fluidSlot : filter) {
        	if(fluidSlot != null)
        		if (GuiUtil.isPointInRegion(guiLeft, guiTop, fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
        			fluidSlot.mouseClicked(player.inventory.getItemStack());
        			break;
        		}
        }
    }
	
	

}
