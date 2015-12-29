package extracells.gui;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.widget.WidgetStorageDirection;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.integration.Integration;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketBusFluidStorage;
import extracells.part.PartFluidStorage;
import extracells.part.PartGasStorage;
import extracells.util.FluidUtil;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class GuiBusFluidStorage extends GuiContainer implements
		WidgetFluidSlot.IConfigurable, IFluidSlotGui {

	private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/storagebusfluid.png");
	private EntityPlayer player;
	private byte filterSize;
	private List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();
	private boolean hasNetworkTool;
	private final PartFluidStorage part;

	public GuiBusFluidStorage(PartFluidStorage _part, EntityPlayer _player) {
		super(new ContainerBusFluidStorage(_part, _player));
		part = _part;
		((ContainerBusFluidStorage) this.inventorySlots).setGui(this);
		this.player = _player;

		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 6; j++) {
				this.fluidSlotList.add(new WidgetFluidSlot(this.player, part, i * 6 + j, 18 * i + 7, 18 * j + 17));
			}
		}

		new PacketBusFluidStorage(this.player, part).sendPacketToServer();
		this.hasNetworkTool = this.inventorySlots.getInventory().size() > 40;
		this.xSize = this.hasNetworkTool ? 246 : 211;
		this.ySize = 222;

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
	}

	public void changeConfig(byte _filterSize) {
		this.filterSize = _filterSize;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX, int mouseY) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 176, 222);
		drawTexturedModalRect(this.guiLeft + 179, this.guiTop, 179, 0, 32, 86);
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
				((WidgetStorageDirection) button).drawTooltip(mouseX, mouseY, (this.width - this.xSize) / 2, (this.height - this.ySize) / 2);
		}
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
	public void initGui() {
		super.initGui();
		this.buttonList.add(new WidgetStorageDirection(0, this.guiLeft - 18, this.guiTop, 16, 16, AccessRestriction.READ_WRITE));
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
}
