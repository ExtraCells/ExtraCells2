package extracells.gui;

import appeng.api.storage.data.IAEFluidStack;
import extracells.Extracells;
import extracells.api.ECApi;
import extracells.container.ContainerGasStorage;
import extracells.gui.widget.FluidWidgetComparator;
import extracells.gui.widget.fluid.AbstractFluidWidget;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.gui.widget.fluid.IFluidSelectorGui;
import extracells.gui.widget.fluid.WidgetFluidSelector;
import extracells.network.packet.part.PacketFluidStorage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiGasStorage extends GuiContainer implements IFluidSelectorGui {

    private final EntityPlayer player;
    private int currentScroll = 0;
    private GuiTextField searchbar;
    private List<AbstractFluidWidget> fluidWidgets = new ArrayList<AbstractFluidWidget>();
    private final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/terminalfluid.png");
    public IAEFluidStack currentFluid;
    private final ContainerGasStorage containerGasStorage;
    private final String guiName;

    public GuiGasStorage(EntityPlayer _player, String _guiName) {
        super(new ContainerGasStorage(_player));
        this.containerGasStorage = (ContainerGasStorage) this.inventorySlots;
        this.containerGasStorage.setGui(this);
        this.player = _player;
        this.xSize = 176;
        this.ySize = 204;
        this.guiName = _guiName;
        new PacketFluidStorage(this.player).sendPacketToServer();
    }

    @Override
    public void handleMouseInput() {
        super.handleMouseInput();
        int deltaWheel = Mouse.getEventDWheel();
        if (deltaWheel < 0) {
            currentScroll++;
        } else if (deltaWheel > 0) {
            currentScroll--;
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(this.guiTexture);
        drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
        this.searchbar.drawTextBox();
        new PacketFluidStorage(this.player).sendPacketToServer();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        this.fontRendererObj.drawString(
                StatCollector.translateToLocal(this.guiName).replace("ME ", ""), 5, 6, 0x000000);
        drawWidgets(mouseX, mouseY);
        if (this.currentFluid != null) {
            long currentFluidAmount = this.currentFluid.getStackSize();
            String amountToText = currentFluidAmount + "mB";
            if (Extracells.shortenedBuckets()) {
                if (currentFluidAmount > 1000000000L) amountToText = currentFluidAmount / 1000000000L + "Mega";
                else if (currentFluidAmount > 1000000L) amountToText = currentFluidAmount / 1000000L + "Kilo";
                else if (currentFluidAmount > 9999L) {
                    amountToText = Long.toString(currentFluidAmount / 1000L);
                }
            }

            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("extracells.tooltip.amount") + ": " + amountToText,
                    45,
                    91,
                    0x000000);
            this.fontRendererObj.drawString(
                    StatCollector.translateToLocal("extracells.tooltip.fluid")
                            + ": "
                            + this.currentFluid.getFluid().getLocalizedName(this.currentFluid.getFluidStack()),
                    45,
                    101,
                    0x000000);
        }
    }

    public void drawWidgets(int mouseX, int mouseY) {
        int listSize = this.fluidWidgets.size();
        if (!this.containerGasStorage.getFluidStackList().isEmpty()) {
            outerLoop:
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 9; x++) {
                    int widgetIndex = y * 9 + x + this.currentScroll * 9;
                    if (0 <= widgetIndex && widgetIndex < listSize) {
                        AbstractFluidWidget widget = this.fluidWidgets.get(widgetIndex);
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
                        this.fluidWidgets.get(widgetIndex).drawTooltip(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
                    } else {
                        break;
                    }
                }
            }
            if (this.currentScroll < 0) this.currentScroll = 0;
            if (listSize / 9 < 4 && this.currentScroll < listSize / 9 + 4) this.currentScroll = 0;
        }
    }

    @Override
    public IFluidSelectorContainer getContainer() {
        return this.containerGasStorage;
    }

    @Override
    public IAEFluidStack getCurrentFluid() {
        return this.containerGasStorage.getSelectedFluidStack();
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

        updateFluids();
        Collections.sort(this.fluidWidgets, new FluidWidgetComparator());
        this.searchbar = new GuiTextField(this.fontRendererObj, this.guiLeft + 81, this.guiTop + 6, 88, 10) {

            private final int xPos = 0;
            private final int yPos = 0;
            private final int width = 0;
            private final int height = 0;

            @Override
            public void mouseClicked(int x, int y, int mouseBtn) {
                boolean flag =
                        x >= this.xPos && x < this.xPos + this.width && y >= this.yPos && y < this.yPos + this.height;
                if (flag && mouseBtn == 3) setText("");
            }
        };
        this.searchbar.setEnableBackgroundDrawing(false);
        this.searchbar.setFocused(true);
        this.searchbar.setMaxStringLength(15);
    }

    @Override
    protected void keyTyped(char key, int keyID) {
        if (keyID == Keyboard.KEY_ESCAPE) this.mc.thePlayer.closeScreen();
        this.searchbar.textboxKeyTyped(key, keyID);
        updateFluids();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
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

    public void updateFluids() {
        this.fluidWidgets = new ArrayList<AbstractFluidWidget>();
        for (IAEFluidStack fluidStack : this.containerGasStorage.getFluidStackList()) {
            if (fluidStack
                            .getFluid()
                            .getLocalizedName(fluidStack.getFluidStack())
                            .toLowerCase()
                            .contains(this.searchbar.getText().toLowerCase())
                    && ECApi.instance().isGas(fluidStack.getFluid())) {
                this.fluidWidgets.add(new WidgetFluidSelector(this, fluidStack));
            }
        }
        updateSelectedFluid();
    }

    public void updateSelectedFluid() {
        this.currentFluid = null;
        for (IAEFluidStack stack : this.containerGasStorage.getFluidStackList()) {
            if (stack.getFluid() == this.containerGasStorage.getSelectedFluid()) this.currentFluid = stack;
        }
    }
}
