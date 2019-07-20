package extracells.gui;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import extracells.container.ContainerBusFluidIO;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.integration.Integration;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketBusFluidIO;
import extracells.part.PartFluidIO;
import extracells.part.PartGasExport;
import extracells.part.PartGasImport;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiBusFluidIO extends ECGuiContainer implements
		WidgetFluidSlot.IConfigurable, IFluidSlotGui {

	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/busiofluid.png");
	private PartFluidIO part;
	private EntityPlayer player;
	private byte filterSize;
	private boolean redstoneControlled;
	private boolean hasNetworkTool;

	public GuiBusFluidIO(PartFluidIO _terminal, EntityPlayer _player) {
		super(new ContainerBusFluidIO(_terminal, _player));
		((ContainerBusFluidIO) this.inventorySlots).setGui(this);
		this.part = _terminal;
		this.player = _player;

		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 0, 61, 21, this, (byte) 2));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 1, 79, 21, this, (byte) 1));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 2, 97, 21, this, (byte) 2));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 3, 61, 39, this, (byte) 1));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 4, 79, 39, this, (byte) 0));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 5, 97, 39, this, (byte) 1));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 6, 61, 57, this, (byte) 2));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 7, 79, 57, this, (byte) 1));
		this.fluidSlotList.add(new WidgetFluidSlot(this.player, this.part, 8, 97, 57, this, (byte) 2));

		new PacketBusFluidIO(this.player, this.part).sendPacketToServer();
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 184;

	}

	@Override
	public void actionPerformed(GuiButton button) {
		super.actionPerformed(button);
		new PacketBusFluidIO(this.player, (byte) button.id, this.part)
				.sendPacketToServer();
	}

	public void changeConfig(byte _filterSize) {
		this.filterSize = _filterSize;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX,
			int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 184);
		drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 86);
		if (this.hasNetworkTool)
			drawTexturedModalRect(this.guiLeft + 179, this.guiTop + 93, 178,
					93, 68, 68);
		for (Object s : this.inventorySlots.inventorySlots) {
			renderBackground((Slot) s);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		boolean overlayRendered = false;
		for (byte i = 0; i < 9; i++) {
			this.fluidSlotList.get(i).drawWidget();
			if (!overlayRendered && this.fluidSlotList.get(i).canRender()) overlayRendered = renderOverlay(this.fluidSlotList.get(i), mouseX, mouseY);
		}

		for (Object button : this.buttonList) {
			if (button instanceof WidgetRedstoneModes)
				((WidgetRedstoneModes) button).drawTooltip(mouseX, mouseY, (this.width - this.xSize) / 2, (this.height - this.ySize) / 2);
		}
		showTooltipList(mouseX, mouseY);
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

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_, int p_146981_3_) {
		return this.func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_);
	}

	protected boolean isPointInRegion(int top, int left, int height, int width, int pointX, int pointY) {
		int k1 = this.guiLeft;
		int l1 = this.guiTop;
		pointX -= k1;
		pointY -= l1;
		return pointX >= top - 1 && pointX < top + height + 1
				&& pointY >= left - 1 && pointY < left + width + 1;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null && slot.getStack() != null && slot.getStack().isItemEqual(AEApi.instance().definitions().items().networkTool().maybeStack(1).get()))
			return;
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
				if((part instanceof PartGasImport || part instanceof PartGasExport) && Integration.Mods.MEKANISMGAS.isEnabled())
					fluidSlot.mouseClickedGas(this.player.inventory.getItemStack());
				else
					fluidSlot.mouseClicked(this.player.inventory.getItemStack());
				break;
			}
		}
	}

	private void renderBackground(Slot slot) {
		if (slot.getStack() == null && (slot.slotNumber < 4 || slot.slotNumber > 39)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
			this.mc.getTextureManager().bindTexture(new ResourceLocation("appliedenergistics2", "textures/guis/states.png"));
			this.drawTexturedModalRect(this.guiLeft + slot.xDisplayPosition, this.guiTop + slot.yDisplayPosition, 240, 208, 16, 16);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_LIGHTING);

		}
	}

	public boolean renderOverlay(WidgetFluidSlot fluidSlot, int mouseX,
			int mouseY) {
		if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			drawGradientRect(fluidSlot.getPosX() + 1, fluidSlot.getPosY() + 1, fluidSlot.getPosX() + 17, fluidSlot.getPosY() + 17, -0x7F000001, -0x7F000001);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			return true;
		}
		return false;
	}

	public void setRedstoneControlled(boolean _redstoneControlled) {
		this.redstoneControlled = _redstoneControlled;
		this.buttonList.clear();
		if (this.redstoneControlled)
			this.buttonList.add(new WidgetRedstoneModes(0, this.guiLeft - 18, this.guiTop, 16, 16, this.part.getRedstoneMode()));
	}

	public boolean shiftClick(ItemStack itemStack) {
		FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (fluidSlot.canRender() && fluid != null && (fluidSlot.getFluid() == null || fluidSlot.getFluid() == fluid)) {
				if((part instanceof PartGasImport || part instanceof PartGasExport) && Integration.Mods.MEKANISMGAS.isEnabled())
					fluidSlot.mouseClickedGas(itemStack);
				else
					fluidSlot.mouseClicked(itemStack);
				return true;
			}
		}
		return false;
	}

	@Override
	public void updateFluids(List<Fluid> fluidList) {
		for (int i = 0; i < this.fluidSlotList.size() && i < fluidList.size(); i++) {
			this.fluidSlotList.get(i).setFluid(fluidList.get(i));
		}
	}

	public void updateRedstoneMode(RedstoneMode mode) {
		if (this.redstoneControlled && this.buttonList.size() > 0)
			((WidgetRedstoneModes) this.buttonList.get(0)).setRedstoneMode(mode);
	}
}
