package extracells.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import appeng.api.AEApi;
import appeng.api.implementations.ICraftingPatternItem;
import extracells.api.IFluidInterface;
import extracells.container.ContainerFluidInterface;
import extracells.gui.widget.WidgetFluidTank;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.registries.BlockEnum;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
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
		for(Object s :this.inventorySlots.inventorySlots){
			renderBackground((Slot) s);
		}
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
		for (WidgetFluidSlot fluidSlot : filter) {
			if(fluidSlot != null){
				int i = fluidSlot.getPosX() + 1;
				int j = fluidSlot.getPosY() + 1;
				if (GuiUtil.isPointInRegion(guiLeft, guiTop, i, j, 16, 16, mouseX, mouseY)) {
					drawRect(i, j, i + 16, j + 16, -2130706433);
					break;
				}
			}
		}
		for(Object s :this.inventorySlots.inventorySlots){
			try {
				renderOutput((Slot) s, mouseX, mouseY);
			} catch (Throwable e) {
				e.printStackTrace();
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
	
	private void renderBackground(Slot slot){
		if (slot.getStack() == null && slot.slotNumber < 9){
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
			this.drawTexturedModalRect(guiLeft + slot.xDisplayPosition, guiTop + slot.yDisplayPosition, 240, 128, 16, 16);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
            
        }
	}
	
	private void renderOutput(Slot slot, int mouseX, int mouseY) throws Throwable{
		if (slot.getStack() != null && slot.slotNumber < 9){
			ItemStack stack = slot.getStack();
			if(stack.getItem() instanceof ICraftingPatternItem){
				ICraftingPatternItem pattern = (ICraftingPatternItem) stack.getItem();
				ItemStack output = pattern.getPatternForItem(stack, Minecraft.getMinecraft().theWorld).getCondensedOutputs()[0].getItemStack().copy();
				
				this.zLevel = 160.0F;
				GL11.glDisable(GL11.GL_LIGHTING);
		        GL11.glEnable(GL11.GL_BLEND);
		        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		        GL11.glColor3f(1, 1, 1);
		        GL11.glDisable(GL11.GL_LIGHTING);
		        GL11.glColor3f(1.0F, 1.0F, 1.0F);
		        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		        drawTexturedModalRect(slot.xDisplayPosition, slot.yDisplayPosition, slot.xDisplayPosition, slot.yDisplayPosition, 18, 18);
		        GL11.glEnable(GL11.GL_LIGHTING);
				
				
				GL11.glTranslatef(0.0F, 0.0F, 32.0F);
		        this.zLevel = 150.0F;
		        RenderItem itemRender = RenderItem.getInstance();
		        itemRender.zLevel = 100.0F;
		        FontRenderer font = null;
		        if (output != null) font = output.getItem().getFontRenderer(output);
		        if (font == null) font = Minecraft.getMinecraft().fontRenderer;
		        GL11.glEnable(GL11.GL_DEPTH_TEST);
		        itemRender.renderItemAndEffectIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), output, slot.xDisplayPosition, slot.yDisplayPosition);
		        itemRender.renderItemOverlayIntoGUI(font, Minecraft.getMinecraft().getTextureManager(), output, slot.xDisplayPosition, slot.yDisplayPosition, null);
		        this.zLevel = 0.0F;
		        itemRender.zLevel = 0.0F;
		        
		        
		        int i = slot.xDisplayPosition;
				int j = slot.yDisplayPosition;
		        if (GuiUtil.isPointInRegion(guiLeft, guiTop, i, j, 16, 16, mouseX, mouseY)){
					GL11.glDisable(GL11.GL_LIGHTING);
		            GL11.glDisable(GL11.GL_DEPTH_TEST);
		            GL11.glColorMask(true, true, true, false);
		            this.drawGradientRect(i, j, i + 16, j + 16, -2130706433, -2130706433);
		            GL11.glColorMask(true, true, true, true);
		            GL11.glEnable(GL11.GL_LIGHTING);
		            GL11.glEnable(GL11.GL_DEPTH_TEST);
				}
			}
		}
	}
}
