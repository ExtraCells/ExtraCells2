package extracells.gui.fluid;

import java.io.IOException;
import java.util.List;

import extracells.gui.IFluidSlotGuiTransfer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import extracells.container.ContainerPlaneFormation;
import extracells.gui.GuiBase;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketPartConfig;
import extracells.part.fluid.PartFluidPlaneFormation;
import extracells.util.FluidHelper;
import extracells.util.NetworkUtil;

public class GuiFluidPlaneFormation extends GuiBase<ContainerPlaneFormation> implements
	IFluidSlotGui, IFluidSlotGuiTransfer {

	private PartFluidPlaneFormation part;
	private EntityPlayer player;
	private WidgetFluidSlot fluidSlot;
	private boolean hasNetworkTool;

	public GuiFluidPlaneFormation(PartFluidPlaneFormation _part,
		EntityPlayer _player) {
		super(new ResourceLocation("extracells", "textures/gui/paneformation.png"), new ContainerPlaneFormation(_part, _player));
		((ContainerPlaneFormation) this.inventorySlots).setGui(this);
		this.part = _part;
		this.player = _player;
		widgetManager.add(fluidSlot = new WidgetFluidSlot(widgetManager, this.part, 0, 79, 39));
		NetworkUtil.sendToPlayer(new PacketPartConfig(part, PacketPartConfig.FLUID_PLANE_FORMATION_INFO), player);
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 184;

	}

	@Override
	protected void drawBackground() {
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 184);
		drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 86);
		if (this.hasNetworkTool) {
			drawTexturedModalRect(this.guiLeft + 179, this.guiTop + 93, 178, 93, 68, 68);
		}
	}

	protected Slot getSlotAtPosition(int p_146975_1_, int p_146975_2_) {
		for (int k = 0; k < this.inventorySlots.inventorySlots.size(); ++k) {
			Slot slot = this.inventorySlots.inventorySlots.get(k);

			if (this.isMouseOverSlot(slot, p_146975_1_, p_146975_2_)) {
				return slot;
			}
		}

		return null;
	}

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_,
		int p_146981_3_) {
		return this.isPointInRegion(p_146981_1_.xPos,
			p_146981_1_.yPos, 16, 16, p_146981_2_, p_146981_3_);
	}

	protected boolean isPointInRegion(int top, int left, int height, int width,
		int pointX, int pointY) {
		int k1 = this.guiLeft;
		int l1 = this.guiTop;
		pointX -= k1;
		pointY -= l1;
		return pointX >= top - 1 && pointX < top + height + 1
			&& pointY >= left - 1 && pointY < left + width + 1;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) throws IOException {
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null
			&& slot.getStack() != null
			&& AEApi.instance().definitions().items().networkTool().isSameAs(slot.getStack())) {
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseBtn);
	}

	@Override
	public boolean shiftClick(ItemStack itemStack) {
		FluidStack containerFluid = FluidHelper.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();

		if (this.fluidSlot.getFluid() == null || fluid != null && this.fluidSlot.getFluid() == fluid) {
			this.fluidSlot.handleFluidContainer(itemStack);
		}
		return true;
	}

	@Override
	public void updateFluids(List<Fluid> fluidList) {
		this.fluidSlot.setFluid(fluidList.get(0));
	}
}
