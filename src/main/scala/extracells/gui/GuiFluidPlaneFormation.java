package extracells.gui;

import appeng.api.AEApi;
import extracells.container.ContainerPlaneFormation;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketFluidPlaneFormation;
import extracells.part.PartFluidPlaneFormation;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class GuiFluidPlaneFormation extends ECGuiContainer implements
		IFluidSlotGui {

	private static final ResourceLocation guiTexture = new ResourceLocation(
			"extracells", "textures/gui/paneformation.png");
	private PartFluidPlaneFormation part;
	private EntityPlayer player;
	private boolean hasNetworkTool;

	public GuiFluidPlaneFormation(PartFluidPlaneFormation _part,
			EntityPlayer _player) {
		super(new ContainerPlaneFormation(_part, _player));
		((ContainerPlaneFormation) this.inventorySlots).setGui(this);
		this.part = _part;
		this.player = _player;
		this.fluidSlot = new WidgetFluidSlot(this.player, this.part, 0, 79, 39);
		new PacketFluidPlaneFormation(this.player, this.part)
				.sendPacketToServer();
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 184;

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

	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		super.drawGuiContainerForegroundLayer(mouseX, mouseY);
		this.fluidSlot.drawWidget();
		renderOverlay(this.fluidSlot, mouseX, mouseY);

		for (Object button : this.buttonList) {
			if (button instanceof WidgetRedstoneModes)
				((WidgetRedstoneModes) button).drawTooltip(this.guiLeft,
						this.guiTop, (this.width - this.xSize) / 2,
						(this.height - this.ySize) / 2);
		}
		showTooltip(mouseX, mouseY);
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

	private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_,
			int p_146981_3_) {
		return this.func_146978_c(p_146981_1_.xDisplayPosition,
				p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_);
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
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		Slot slot = getSlotAtPosition(mouseX, mouseY);

		if (slot != null
				&& slot.getStack() != null
				&& AEApi.instance().definitions().items().networkTool().isSameAs(slot.getStack()))
			return;
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		if (isPointInRegion(this.fluidSlot.getPosX(), this.fluidSlot.getPosY(),
				18, 18, mouseX, mouseY))
			this.fluidSlot.mouseClicked(this.player.inventory.getItemStack());
	}

	public boolean renderOverlay(WidgetFluidSlot fluidSlot, int mouseX,
			int mouseY) {
		if (isPointInRegion(fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18,
				mouseX, mouseY)) {
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			drawGradientRect(fluidSlot.getPosX() + 1, fluidSlot.getPosY() + 1,
					fluidSlot.getPosX() + 17, fluidSlot.getPosY() + 17,
					-0x7F000001, -0x7F000001);
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			return true;
		}
		return false;
	}

	public void shiftClick(ItemStack itemStack) {
		FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
		Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();

		if (this.fluidSlot.getFluid() == null || fluid != null
				&& this.fluidSlot.getFluid() == fluid)
			this.fluidSlot.mouseClicked(itemStack);
	}

	@Override
	public void updateFluids(List<Fluid> fluidList) {
		this.fluidSlot.setFluid(fluidList.get(0));
	}
}
