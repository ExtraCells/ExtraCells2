package extracells.gui;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.client.gui.widgets.GuiTabButton;
import appeng.client.gui.widgets.ITooltip;
import appeng.core.localization.GuiText;
import codechicken.nei.VisiblityData;
import codechicken.nei.api.INEIGuiHandler;
import codechicken.nei.api.TaggedInventoryArea;
import cpw.mods.fml.common.Optional;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.widget.WidgetStorageDirection;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.integration.Integration;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.other.PacketGuiSwitch;
import extracells.network.packet.part.PacketBusFluidStorage;
import extracells.part.PartFluidStorage;
import extracells.part.PartGasStorage;
import extracells.util.FluidUtil;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Collections;
import java.util.List;

@Optional.Interface(modid = "NotEnoughItems", iface = "codechicken.nei.api.INEIGuiHandler")
public class GuiBusFluidStorage extends ECGuiContainer implements
		WidgetFluidSlot.IConfigurable, IFluidSlotGui, INEIGuiHandler {

	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/storagebusfluid.png");
	private final EntityPlayer player;
	private byte filterSize;
	private final boolean hasNetworkTool;
	private final PartFluidStorage part;
	private GuiTabButton priority;

	public GuiBusFluidStorage(PartFluidStorage _part, EntityPlayer _player) {
		super(new ContainerBusFluidStorage(_part, _player));
		part = _part;
		((ContainerBusFluidStorage) this.inventorySlots).setGui(this);
		this.player = _player;

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 6; j++) {
				this.fluidSlotList.add(new WidgetFluidSlot(this.player, part, i * 6 + j, 18 * i + 7, 18 * j + 30));
			}
		}

		new PacketBusFluidStorage(this.player, part).sendPacketToServer();
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 235;
	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		if (button instanceof WidgetStorageDirection) {
			switch (((WidgetStorageDirection) button).getAccessRestriction()) {
			case NO_ACCESS:
				new PacketBusFluidStorage(this.player, AccessRestriction.READ, false).sendPacketToServer();
				break;
			case READ:
				new PacketBusFluidStorage(this.player, AccessRestriction.READ_WRITE, false).sendPacketToServer();
				break;
			case READ_WRITE:
				new PacketBusFluidStorage(this.player, AccessRestriction.WRITE, false).sendPacketToServer();
				break;
			case WRITE:
				new PacketBusFluidStorage(this.player, AccessRestriction.NO_ACCESS, false).sendPacketToServer();
				break;
			default:
				break;
			}
		}
		else if (button == this.priority) {
			new PacketGuiSwitch(100 + part.getSide().ordinal(), part.getHostTile()).sendPacketToServer();
		}
	}

	public void changeConfig(byte _filterSize) {
		this.filterSize = _filterSize;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 235);
		drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 86);
		fontRendererObj.drawString(StatCollector.translateToLocal("extracells.gui.fluid.storage"), 8 + this.guiLeft, 6 + this.guiTop, 4210752);
		if (this.hasNetworkTool)
			drawTexturedModalRect(this.guiLeft + 179, this.guiTop + 93, 178, 93, 68, 68);
		for (Object s : this.inventorySlots.inventorySlots) {
			renderBackground((Slot) s);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		boolean overlayRendered = false;
		for (byte i = 0; i < 54; i++) {
			this.fluidSlotList.get(i).drawWidget();
			if (!overlayRendered && this.fluidSlotList.get(i).canRender())
				overlayRendered = GuiUtil.renderOverlay(this.zLevel, this.guiLeft, this.guiTop, this.fluidSlotList.get(i), mouseX, mouseY);
		}
		for (Object button : this.buttonList) {
			if (button instanceof WidgetStorageDirection)
			{
				((WidgetStorageDirection) button).drawTooltip(mouseX, mouseY, (this.width - this.xSize) / 2, (this.height - this.ySize) / 2);
			}
		}
		showTooltipList(mouseX, mouseY);
	}

	@Override
	public void drawScreen( final int mouseX, final int mouseY, final float btn )
	{
		super.drawScreen( mouseX, mouseY, btn );
		for( final Object c : this.buttonList )
		{
			if( c instanceof ITooltip )
			{
				handleTooltip(mouseX, mouseY, (ITooltip) c);
			}
		}
	}

	protected void handleTooltip(int mouseX, int mouseY, ITooltip c) {
		final int x = c.xPos(); // ((GuiImgButton) c).xPosition;
		int y = c.yPos(); // ((GuiImgButton) c).yPosition;

		if( x < mouseX && x + c.getWidth() > mouseX && c.isVisible() )
		{
			if( y < mouseY && y + c.getHeight() > mouseY)
			{
				if( y < 15 )
				{
					y = 15;
				}

				final String msg = c.getMessage();
				if( msg != null )
				{
					this.drawTooltip( x + 11, y + 4, 0, msg );
				}
			}
		}
	}

	public void drawTooltip( final int par2, final int par3, final int forceWidth, final String message )
	{
		GL11.glPushAttrib( GL11.GL_ALL_ATTRIB_BITS );
		GL11.glDisable( GL12.GL_RESCALE_NORMAL );
		RenderHelper.disableStandardItemLighting();
		GL11.glDisable( GL11.GL_LIGHTING );
		GL11.glDisable( GL11.GL_DEPTH_TEST );
		final String[] var4 = message.split( "\n" );

		if( var4.length > 0 )
		{
			int var5 = 0;
			int var6;
			int var7;

			for( var6 = 0; var6 < var4.length; ++var6 )
			{
				var7 = this.fontRendererObj.getStringWidth( var4[var6] );

				if( var7 > var5 )
				{
					var5 = var7;
				}
			}

			var6 = par2 + 12;
			var7 = par3 - 12;
			int var9 = 8;

			if( var4.length > 1 )
			{
				var9 += 2 + ( var4.length - 1 ) * 10;
			}

			if( this.guiTop + var7 + var9 + 6 > this.height )
			{
				var7 = this.height - var9 - this.guiTop - 6;
			}

			if( forceWidth > 0 )
			{
				var5 = forceWidth;
			}

			this.zLevel = 300.0F;
			itemRender.zLevel = 300.0F;
			final int var10 = -267386864;
			this.drawGradientRect( var6 - 3, var7 - 4, var6 + var5 + 3, var7 - 3, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 3, var6 + var5 + 3, var7 + var9 + 4, var10, var10 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 - 4, var7 - 3, var6 - 3, var7 + var9 + 3, var10, var10 );
			this.drawGradientRect( var6 + var5 + 3, var7 - 3, var6 + var5 + 4, var7 + var9 + 3, var10, var10 );
			final int var11 = 1347420415;
			final int var12 = ( var11 & 16711422 ) >> 1 | var11 & -16777216;
			this.drawGradientRect( var6 - 3, var7 - 3 + 1, var6 - 3 + 1, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 + var5 + 2, var7 - 3 + 1, var6 + var5 + 3, var7 + var9 + 3 - 1, var11, var12 );
			this.drawGradientRect( var6 - 3, var7 - 3, var6 + var5 + 3, var7 - 3 + 1, var11, var11 );
			this.drawGradientRect( var6 - 3, var7 + var9 + 2, var6 + var5 + 3, var7 + var9 + 3, var12, var12 );

			for( int var13 = 0; var13 < var4.length; ++var13 )
			{
				String var14 = var4[var13];

				if( var13 == 0 )
				{
					var14 = '\u00a7' + Integer.toHexString( 15 ) + var14;
				}
				else
				{
					var14 = "\u00a77" + var14;
				}

				this.fontRendererObj.drawStringWithShadow( var14, var6, var7, -1 );

				if( var13 == 0 )
				{
					var7 += 2;
				}

				var7 += 10;
			}

			this.zLevel = 0.0F;
			itemRender.zLevel = 0.0F;
		}
		GL11.glPopAttrib();
	}

	@Override
	public byte getConfigState() {
		return this.filterSize;
	}

	protected Slot getSlotAtPosition(int p_146975_1_, int p_146975_2_) {
		for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = (Slot) this.inventorySlots.inventorySlots.get(k);

			if (this.isMouseOverSlot(slot, p_146975_1_, p_146975_2_)) {
				return slot;
			}
		}

		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void initGui() {
		super.initGui();
		this.buttonList.add(new WidgetStorageDirection(0, this.guiLeft - 18, this.guiTop, 16, 16, AccessRestriction.READ_WRITE));
		this.priority = new GuiTabButton(this.guiLeft + 154, this.guiTop, 2 + 4 * 16, GuiText.Priority.getLocal(), itemRender);
		this.buttonList.add(priority);
	}

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_, int p_146981_3_) {
		return this.func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null && slot.getStack() != null && AEApi.instance().definitions().items().networkTool().isSameAs(slot.getStack()))
			return;
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (GuiUtil.isPointInRegion(this.guiLeft, this.guiTop, fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
				if(part instanceof PartGasStorage && Integration.Mods.MEKANISMGAS.isEnabled())
					fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
				else
					fluidSlot.mouseClicked(this.player.inventory.getItemStack());
				break;
			}
		}
	}

	private void renderBackground(Slot slot) {
		if (slot.getStack() == null && (slot.slotNumber == 0 || slot.slotNumber > 36)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, 240, 208, 16, 16);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);
		}
	}

	public void shiftClick(ItemStack itemStack) {
		FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (fluidSlot.getFluid() == null || fluid != null && fluidSlot.getFluid() == fluid) {
				if(part instanceof PartGasStorage && Integration.Mods.MEKANISMGAS.isEnabled())
					fluidSlot.mouseClickedGas(itemStack);
				else
					fluidSlot.mouseClicked(itemStack);
				return;
			}
		}
	}

	public void updateAccessRestriction(AccessRestriction mode) {
		if (this.buttonList.size() > 0)
			((WidgetStorageDirection) this.buttonList.get(0)).setAccessRestriction(mode);
	}

	@Override
	public void updateFluids(List<Fluid> fluidList) {
		for (int i = 0; i < this.fluidSlotList.size() && i < fluidList.size(); i++) {
			this.fluidSlotList.get(i).setFluid(fluidList.get(i));
		}
	}

	@Override
	public VisiblityData modifyVisiblity(GuiContainer guiContainer, VisiblityData visiblityData) {
		return visiblityData;
	}

	@Override
	public Iterable<Integer> getItemSpawnSlots(GuiContainer guiContainer, ItemStack itemStack) {
		return Collections.emptyList();
	}

	@Override
	public List<TaggedInventoryArea> getInventoryAreas(GuiContainer guiContainer) {
		return null;
	}

	@Override
	public boolean handleDragNDrop(GuiContainer gui, int mouseX, int mouseY, ItemStack draggedStack, int button) {
		if (!(gui instanceof GuiBusFluidStorage) || draggedStack == null) {
			return false;
		}
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (GuiUtil.isPointInRegion(this.guiLeft, this.guiTop, fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
				if(part instanceof PartGasStorage && Integration.Mods.MEKANISMGAS.isEnabled())
					fluidSlot.mouseNEIClickedGas(draggedStack);
				else
					fluidSlot.mouseNEIClicked(draggedStack);
				break;
			}
		}
		draggedStack.stackSize = 0;
		return true;
	}

	@Override
	public boolean hideItemPanelSlot(GuiContainer gui, int x, int y, int w, int h) {
		return false;
	}
}
