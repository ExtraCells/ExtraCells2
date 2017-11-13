package extracells.gui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.container.ContainerStorage;
import extracells.container.IFluidSelectorContainer;
import extracells.container.StorageType;
import extracells.gui.widget.FluidWidgetComparator;
import extracells.gui.widget.fluid.AbstractFluidWidget;
import extracells.gui.widget.fluid.IFluidSelectorGui;
import extracells.gui.widget.fluid.WidgetFluidSelector;
import extracells.network.packet.part.PacketStorageOpenContainer;
import extracells.util.ECConfigHandler;
import extracells.util.NetworkUtil;

public class GuiStorage extends GuiContainer implements IFluidSelectorGui {

	private StorageType storageType;
	private int currentScroll = 0;
	private GuiTextField searchbar;
	private List<AbstractFluidWidget> fluidWidgets = new ArrayList<AbstractFluidWidget>();
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/terminalfluid.png");
	public IAEFluidStack currentFluid;
	private ContainerStorage container;
	private final String guiName;

	public GuiStorage(ContainerStorage container, String guiName) {
		super(container);
		this.container = container;
		this.storageType = container.getStorageType();
		this.xSize = 176;
		this.ySize = 204;
		this.guiName = guiName;
		NetworkUtil.sendToServer(new PacketStorageOpenContainer());
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY) {
		drawDefaultBackground();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize,
			this.ySize);
		this.searchbar.drawTextBox();
		//TODO: ?????
		//new PacketFluidStorage(this.player).sendPacketToServer();
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(I18n.translateToLocal(this.guiName).replace("ME ", ""), 5, 6, 0x000000);
		drawWidgets(mouseX, mouseY);
		if (this.currentFluid != null) {
			long currentFluidAmount = this.currentFluid.getStackSize();
			String amountToText = Long.toString(currentFluidAmount) + "mB";
			if (ECConfigHandler.shortenedBuckets) {
				if (currentFluidAmount > 1000000000L) {
					amountToText = Long.toString(currentFluidAmount / 1000000000L) + storageType.getMega();
				} else if (currentFluidAmount > 1000000L) {
					amountToText = Long.toString(currentFluidAmount / 1000000L) + storageType.getKilo();
				} else if (currentFluidAmount > 9999L) {
					amountToText = Long.toString(currentFluidAmount / 1000L) + storageType.getBuckets();
				}
			}

			this.fontRenderer.drawString(
				I18n.translateToLocal("extracells.tooltip.amount")
					+ ": " + amountToText, 45, 91, 0x000000);
			this.fontRenderer.drawString(
				I18n.translateToLocal("extracells.tooltip.fluid")
					+ ": "
					+ this.currentFluid.getFluid().getLocalizedName(
					this.currentFluid.getFluidStack()), 45, 101,
				0x000000);
		}
	}

	public void drawWidgets(int mouseX, int mouseY) {
		int listSize = this.fluidWidgets.size();
		if (!this.container.getFluidStackList().isEmpty()) {
			outerLoop:
			for (int y = 0; y < 4; y++) {
				for (int x = 0; x < 9; x++) {
					int widgetIndex = y * 9 + x + this.currentScroll * 9;
					if (0 <= widgetIndex && widgetIndex < listSize) {
						AbstractFluidWidget widget = this.fluidWidgets
							.get(widgetIndex);
						widget.drawWidget(x * 18 + 7, y * 18 + 17);
					} else {
						break outerLoop;
					}
				}
			}

			for (int x = 0; x < 9; x++) {
				for (int y = 0; y < 4; y++) {
					int widgetIndex = y * 9 + x;
					if (0 <= widgetIndex && widgetIndex < listSize) {
						this.fluidWidgets.get(widgetIndex).drawTooltip(
							x * 18 + 7, y * 18 - 1, mouseX, mouseY);
					} else {
						break;
					}
				}
			}

			int deltaWheel = Mouse.getDWheel();
			if (deltaWheel > 0) {
				this.currentScroll++;
			} else if (deltaWheel < 0) {
				this.currentScroll--;
			}

			if (this.currentScroll < 0) {
				this.currentScroll = 0;
			}
			if (listSize / 9 < 4 && this.currentScroll < listSize / 9 + 4) {
				this.currentScroll = 0;
			}
		}
	}

	@Override
	public IFluidSelectorContainer getContainer() {
		return this.container;
	}

	@Override
	public IAEFluidStack getCurrentFluid() {
		return this.container.getSelectedFluidStack();
	}

	@Override
	public int guiLeft() {
		return this.guiLeft;
	}

	@Override
	public int guiTop() {
		return this.guiTop;
	}

	@Override
	public void initGui() {
		super.initGui();
		Mouse.getDWheel();

		updateFluids();
		Collections.sort(this.fluidWidgets, new FluidWidgetComparator());
		this.searchbar = new GuiTextField(0, this.fontRenderer,
			this.guiLeft + 81, this.guiTop + 6, 88, 10) {

			private int xPos = 0;
			private int yPos = 0;
			private int width = 0;
			private int height = 0;

			@Override
			public boolean mouseClicked(int x, int y, int mouseBtn) {
				boolean flag = x >= this.xPos && x < this.xPos + this.width
					&& y >= this.yPos && y < this.yPos + this.height;
				if (flag && mouseBtn == 3) {
					setText("");
				}
				return flag;
			}
		};
		this.searchbar.setEnableBackgroundDrawing(false);
		this.searchbar.setFocused(true);
		this.searchbar.setMaxStringLength(15);
	}

	@Override
	protected void keyTyped(char key, int keyID) {
		if (keyID == Keyboard.KEY_ESCAPE) {
			this.mc.player.closeScreen();
		}
		this.searchbar.textboxKeyTyped(key, keyID);
		updateFluids();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseBtn);
		this.searchbar.mouseClicked(mouseX, mouseY, mouseBtn);
		int listSize = this.fluidWidgets.size();
		for (int x = 0; x < 9; x++) {
			for (int y = 0; y < 4; y++) {
				int index = y * 9 + x;
				if (0 <= index && index < listSize) {
					AbstractFluidWidget widget = this.fluidWidgets.get(index);
					widget.mouseClicked(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
				}
			}
		}
	}

	public void updateFluids(IItemList<IAEFluidStack> fluidStacks) {
		container.updateFluidList(fluidStacks);
		updateFluids();
	}

	public void updateFluids() {
		this.fluidWidgets = new ArrayList<AbstractFluidWidget>();
		for (IAEFluidStack fluidStack : this.container.getFluidStackList()) {
			if (fluidStack.getFluid().getLocalizedName(fluidStack.getFluidStack()).toLowerCase().contains(this.searchbar.getText().toLowerCase()) && storageType.canSee(fluidStack.getFluidStack())) {
				this.fluidWidgets.add(new WidgetFluidSelector(this, fluidStack));
			}
		}
		updateSelectedFluid();
	}

	public void updateSelectedFluid() {
		this.currentFluid = null;
		for (IAEFluidStack stack : this.container.getFluidStackList()) {
			if (stack.getFluid() == this.container.getSelectedFluid()) {
				this.currentFluid = stack;
			}
		}
	}
}
