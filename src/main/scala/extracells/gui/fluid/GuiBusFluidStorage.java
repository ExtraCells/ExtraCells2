package extracells.gui.fluid;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import extracells.gui.IFluidSlotGuiTransfer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import extracells.container.fluid.ContainerBusFluidStorage;
import extracells.gui.GuiBase;
import extracells.gui.ISlotRenderer;
import extracells.gui.SlotUpgradeRenderer;
import extracells.gui.buttons.ButtonStorageDirection;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketPartConfig;
import extracells.part.fluid.PartFluidStorage;
import extracells.part.gas.PartGasStorage;
import extracells.registries.PartEnum;
import extracells.util.FluidHelper;
import extracells.util.NetworkUtil;

public class GuiBusFluidStorage extends GuiBase<ContainerBusFluidStorage> implements WidgetFluidSlot.IConfigurable, IFluidSlotGui, IFluidSlotGuiTransfer {

	private EntityPlayer player;
	private byte filterSize;
	private List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();
	private boolean hasNetworkTool;
	private final PartFluidStorage part;

	public GuiBusFluidStorage(PartFluidStorage part, EntityPlayer _player) {
		super(new ResourceLocation("extracells", "textures/gui/storagebusfluid.png"), new ContainerBusFluidStorage(part, _player));
		this.part = part;
		container.setGui(this);
		this.player = _player;

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 6; j++) {
				WidgetFluidSlot fluidSlot = new WidgetFluidSlot(widgetManager, this.part, i * 6 + j, 18 * i + 7, 18 * j + 17);
				fluidSlotList.add(fluidSlot);
				widgetManager.add(fluidSlot);
			}
		}

		NetworkUtil.sendToServer(new PacketPartConfig(this.part, PacketPartConfig.FLUID_STORAGE_INFO));
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 222;
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button instanceof ButtonStorageDirection) {
			AccessRestriction restriction;
			switch (((ButtonStorageDirection) button).getAccessRestriction()) {
				case NO_ACCESS:
					restriction = AccessRestriction.READ;
					break;
				case READ:
					restriction = AccessRestriction.READ_WRITE;
					break;
				case READ_WRITE:
					restriction = AccessRestriction.WRITE;
					break;
				case WRITE:
					restriction = AccessRestriction.NO_ACCESS;
					break;
				default:
					restriction = null;
					break;
			}
			if (restriction != null) {
				NetworkUtil.sendToServer(new PacketPartConfig(part, PacketPartConfig.FLUID_STORAGE_ACCESS, restriction.toString()));
			}
		}
	}

	public void changeConfig(byte _filterSize) {
		this.filterSize = _filterSize;
	}

	@Override
	protected void drawBackground() {
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 222);
		drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 86);
		if (this.hasNetworkTool) {
			drawTexturedModalRect(this.guiLeft + 179, this.guiTop + 93, 178, 93, 68, 68);
		}
	}

	@Override
	protected boolean hasSlotRenders() {
		return true;
	}

	@Nullable
	@Override
	protected ISlotRenderer getSlotRenderer(Slot slot) {
		if ((slot.getStack() == null || slot.getStack().isEmpty()) && (slot.slotNumber < 1 || slot.slotNumber > 37)) {
			return SlotUpgradeRenderer.INSTANCE;
		}
		return null;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);

		PartEnum partEnum = part instanceof PartGasStorage ? PartEnum.GASSTORAGE : PartEnum.FLUIDSTORAGE;
		fontRenderer.drawString(partEnum.getStatName().replace("ME ", ""), 8, 6, 4210752);
		fontRenderer.drawString(player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 3, 4210752);
	}

	@Override
	public byte getConfigState() {
		return this.filterSize;
	}

	protected Slot getSlotAtPosition(int mouseX, int mouseY) {
		for (Slot slot : container.inventorySlots) {
			if (this.isMouseOverSlot(slot, mouseX, mouseY)) {
				return slot;
			}
		}

		return null;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.add(new ButtonStorageDirection(0, this.guiLeft - 18, this.guiTop, 16, 16, AccessRestriction.READ_WRITE));
	}

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_, int p_146981_3_) {
		return this.isPointInRegion(p_146981_1_.xPos, p_146981_1_.yPos, 16, 16, p_146981_2_, p_146981_3_);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) throws IOException {
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null && slot.getStack() != null && AEApi.instance().definitions().items().networkTool().isSameAs(slot.getStack())) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseBtn);
	}

	@Override
	public boolean shiftClick(ItemStack itemStack) {
		FluidStack containerFluid = FluidHelper.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
		for (WidgetFluidSlot fluidSlot : this.fluidSlotList) {
			if (fluid != null && (fluidSlot.getFluid() == null || fluidSlot.getFluid() == fluid)) {
				fluidSlot.handleContainer(itemStack);
				return true;
			}
		}
		return false;
	}

	public void updateAccessRestriction(AccessRestriction mode) {
		if (this.buttonList.size() > 0) {
			((ButtonStorageDirection) this.buttonList.get(0)).setAccessRestriction(mode);
		}
	}

	@Override
	public void updateFluids(List<Fluid> fluidList) {
		for (int i = 0; i < this.fluidSlotList.size() && i < fluidList.size(); i++) {
			this.fluidSlotList.get(i).setFluid(fluidList.get(i));
		}
	}
}
