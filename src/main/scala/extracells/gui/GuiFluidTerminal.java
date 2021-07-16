package extracells.gui;

import appeng.api.storage.data.IAEFluidStack;
import appeng.client.gui.AEBaseGui;
import appeng.client.gui.widgets.GuiScrollbar;
import extracells.api.ECApi;
import extracells.container.ContainerFluidTerminal;
import extracells.gui.widget.FluidWidgetComparator;
import extracells.gui.widget.fluid.AbstractFluidWidget;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.gui.widget.fluid.IFluidSelectorGui;
import extracells.gui.widget.fluid.WidgetFluidSelector;
import extracells.network.packet.part.PacketFluidTerminal;
import extracells.part.PartFluidTerminal;
import extracells.util.FluidUtil;
import net.minecraft.client.Minecraft;
import appeng.client.gui.widgets.MEGuiTextField;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiFluidTerminal extends AEBaseGui implements IFluidSelectorGui {

	private final int COLUMNS = 9;
	private final int ROWS = 4;

	private PartFluidTerminal terminal;
	private EntityPlayer player;
	private MEGuiTextField searchbar;
	private List<AbstractFluidWidget> fluidWidgets = new ArrayList<AbstractFluidWidget>();
	private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/terminalfluid.png");
	public IAEFluidStack currentFluid;
	private ContainerFluidTerminal containerTerminalFluid;

	public GuiFluidTerminal(PartFluidTerminal _terminal, EntityPlayer _player) {
		super(new ContainerFluidTerminal(_terminal, _player));

		final GuiScrollbar scrollbar = new GuiScrollbar();
		this.setScrollBar(scrollbar);

		this.containerTerminalFluid = (ContainerFluidTerminal) this.inventorySlots;
		this.containerTerminalFluid.setGui(this);
		this.terminal = _terminal;
		this.player = _player;
		this.xSize = 195;
		this.ySize = 204;
		new PacketFluidTerminal(this.player, this.terminal).sendPacketToServer();
	}

	@Override
	public void drawBG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
		drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.searchbar.drawTextBox();
	}

	@Override
	public void drawFG(final int offsetX, final int offsetY, final int mouseX, final int mouseY) {
		String name = StatCollector
				.translateToLocal("extracells.part.fluid.terminal.name")
				.replace("ME ", "");

		this.fontRendererObj.drawString(name, 9, 6, 0x000000);

		if (this.currentFluid != null) {
			long currentFluidAmount = this.currentFluid.getStackSize();
			String amountToText = FluidUtil.getAmountAsPrettyString(currentFluidAmount);

			String amountText = StatCollector
					.translateToLocal("extracells.tooltip.amount") + ": " + amountToText;

			String fluidText = StatCollector
					.translateToLocal("extracells.tooltip.fluid") + ": " +
					this.currentFluid.getFluid().getLocalizedName(this.currentFluid.getFluidStack());

			this.fontRendererObj.drawString(amountText, 45, 91, 0x000000);
			this.fontRendererObj.drawString(fluidText, 45, 101, 0x000000);
		}

		drawWidgets(mouseX, mouseY);
	}

	public void drawWidgets(int mouseX, int mouseY) {
		int listSize = this.fluidWidgets.size();

		if (!this.containerTerminalFluid.getFluidStackList().isEmpty()) {
			for (int x = 0; x < COLUMNS; x++) {
				for (int y = 0; y < ROWS; y++) {
					int widgetIndex = getWidgetIndexByCellCoordinate(x, y);
					if (widgetIndex < listSize) {
						AbstractFluidWidget widget = this.fluidWidgets.get(widgetIndex);
						widget.drawWidget(
								x * WidgetFluidSelector.SIZE + 7,
								y * WidgetFluidSelector.SIZE + 17
						);
					} else {
						break;
					}
				}
			}

			for (int x = 0; x < COLUMNS; x++) {
				for (int y = 0; y < ROWS; y++) {
					int widgetIndex = getWidgetIndexByCellCoordinate(x, y);
					if (widgetIndex < listSize) {
						boolean tooltipRes = this.fluidWidgets.get(widgetIndex).drawTooltip(
								x * WidgetFluidSelector.SIZE + 7,
								y * WidgetFluidSelector.SIZE - 1,
								mouseX,
								mouseY
						);
						if (tooltipRes) {
							break;
						}
					} else {
						break;
					}
				}
			}
		}
	}

	int getWidgetIndexByCellCoordinate(int x, int y) {
		return (y + this.getScrollBar().getCurrentScroll()) * COLUMNS + x;
	}

	@Override
	public IFluidSelectorContainer getContainer() {
		return this.containerTerminalFluid;
	}

	@Override
	public IAEFluidStack getCurrentFluid() {
		return this.currentFluid;
	}

	public PartFluidTerminal getTerminal() {
		return this.terminal;
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

		this.getScrollBar().setLeft(175);
		this.getScrollBar().setHeight(70);
		this.getScrollBar().setTop(18);

		Mouse.getDWheel();

		updateFluids();
		Collections.sort(this.fluidWidgets, new FluidWidgetComparator());
		this.searchbar = new MEGuiTextField(this.fontRendererObj, this.guiLeft + 79, this.guiTop + 4, 84, 10);

		this.searchbar.setEnableBackgroundDrawing(false);
		this.searchbar.setFocused(true);
		this.searchbar.setMaxStringLength(15);
	}

	@Override
	protected void keyTyped(char key, int keyID) {
		if (keyID == Keyboard.KEY_ESCAPE) {
			this.mc.thePlayer.closeScreen();
		}

		if (searchbar.isFocused()) {
			this.searchbar.textboxKeyTyped(key, keyID);
			updateFluids();
			return;
		}

		super.keyTyped(key, keyID);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
		this.searchbar.mouseClicked(mouseX, mouseY, mouseBtn);

		// Clear search field on right click (Same as in AE Terminals)
		if (mouseBtn == 1 && this.searchbar.isMouseIn(mouseX, mouseY)) {
			this.searchbar.setText("");
			updateFluids();
		}

		int listSize = this.fluidWidgets.size();

		for (int x = 0; x < COLUMNS; x++) {
			for (int y = 0; y < ROWS; y++) {
				int widgetIndex = getWidgetIndexByCellCoordinate(x, y);
				if (widgetIndex < listSize) {
					AbstractFluidWidget widget = this.fluidWidgets.get(widgetIndex);
					widget.mouseClicked(
							x * WidgetFluidSelector.SIZE + 7,
							y * WidgetFluidSelector.SIZE - 1,
							mouseX,
							mouseY
					);
				}
			}
		}

		super.mouseClicked(mouseX, mouseY, mouseBtn);
	}

	public void updateFluids() {
		this.fluidWidgets = new ArrayList<AbstractFluidWidget>();
		for (IAEFluidStack fluidStack : this.containerTerminalFluid.getFluidStackList()) {
			if (fluidStack.getFluid().getLocalizedName(fluidStack.getFluidStack()).toLowerCase().contains(this.searchbar.getText().toLowerCase()) && ECApi.instance().canFluidSeeInTerminal(
					fluidStack.getFluid())) {
				this.fluidWidgets.add(new WidgetFluidSelector(this, fluidStack));
			}
		}
		updateSelectedFluid();

		this.getScrollBar().setRange(0, ((this.fluidWidgets.size() + COLUMNS - 1) / COLUMNS) - ROWS, 1);
	}

	public void updateSelectedFluid() {
		this.currentFluid = null;
		for (IAEFluidStack stack : this.containerTerminalFluid.getFluidStackList()) {
			if (stack.getFluid() == this.containerTerminalFluid.getSelectedFluid())
				this.currentFluid = stack;
		}
	}
}
