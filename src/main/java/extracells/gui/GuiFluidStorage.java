package extracells.gui;

import appeng.api.storage.data.IAEFluidStack;
import extracells.Extracells;
import extracells.container.ContainerFluidStorage;
import extracells.gui.widget.FluidWidgetComparator;
import extracells.gui.widget.fluid.AbstractFluidWidget;
import extracells.gui.widget.fluid.IFluidSelectorContainer;
import extracells.gui.widget.fluid.IFluidSelectorGui;
import extracells.gui.widget.fluid.WidgetFluidSelector;
import extracells.network.packet.part.PacketFluidStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GuiFluidStorage extends GuiContainer implements IFluidSelectorGui {

    private EntityPlayer player;
    private int currentScroll = 0;
    private GuiTextField searchbar;
    private List<AbstractFluidWidget> fluidWidgets = new ArrayList<AbstractFluidWidget>();
    private ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/terminalfluid.png");
    public IAEFluidStack currentFluid;
    private ContainerFluidStorage containerFluidStorage;

    public GuiFluidStorage(EntityPlayer _player) {
        super(new ContainerFluidStorage(_player));
        containerFluidStorage = (ContainerFluidStorage) inventorySlots;
        containerFluidStorage.setGui(this);
        player = _player;
        xSize = 176;
        ySize = 204;
        new PacketFluidStorage(player).sendPacketToServer();
    }

    public void updateFluids() {
        fluidWidgets = new ArrayList<AbstractFluidWidget>();
        for (IAEFluidStack fluidStack : containerFluidStorage.getFluidStackList()) {
            fluidWidgets.add(new WidgetFluidSelector(this, fluidStack));
        }
        updateSelectedFluid();
    }

    public void updateSelectedFluid() {
        currentFluid = null;
        for (IAEFluidStack stack : containerFluidStorage.getFluidStackList()) {
            if (stack.getFluid() == containerFluidStorage.getSelectedFluid())
                currentFluid = stack;
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        Mouse.getDWheel();

        updateFluids();
        Collections.sort(fluidWidgets, new FluidWidgetComparator());
        searchbar = new GuiTextField(fontRendererObj, guiLeft + 81, guiTop - 12, 88, 10) {

            private int xPos = 0;
            private int yPos = 0;
            private int width = 0;
            private int height = 0;

            public void mouseClicked(int x, int y, int mouseBtn) {
                boolean flag = x >= xPos && x < xPos + width && y >= yPos && y < yPos + height;
                if (flag && mouseBtn == 3)
                    setText("");
            }
        };
        searchbar.setEnableBackgroundDrawing(false);
        searchbar.setFocused(true);
        searchbar.setMaxStringLength(15);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float alpha, int sizeX, int sizeY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop - 18, 0, 0, xSize, ySize);
        searchbar.drawTextBox();
        new PacketFluidStorage(player).sendPacketToServer();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        fontRendererObj.drawString("extracells.part.fluid.terminal".replace("ME ", ""), 5, -12, 0x000000);
        drawWidgets(mouseX, mouseY);
        if (currentFluid != null) {
            long currentFluidAmount = currentFluid.getStackSize();
            String amountToText = Long.toString(currentFluidAmount) + "mB";
            if (Extracells.shortenedBuckets) {
                if (currentFluidAmount > 1000000000L)
                    amountToText = Long.toString(currentFluidAmount / 1000000000L) + "MegaB";
                else if (currentFluidAmount > 1000000L)
                    amountToText = Long.toString(currentFluidAmount / 1000000L) + "KiloB";
                else if (currentFluidAmount > 9999L) {
                    amountToText = Long.toString(currentFluidAmount / 1000L) + "B";
                }
            }

            fontRendererObj.drawString(StatCollector.translateToLocal("extracells.tooltip.amount") + ": " + amountToText, 45, 73, 0x000000);
            fontRendererObj.drawString(StatCollector.translateToLocal("extracells.tooltip.fluid") + ": " + currentFluid.getFluid().getLocalizedName(currentFluid.getFluidStack()), 45, 83, 0x000000);
        }
    }

    public void drawWidgets(int mouseX, int mouseY) {
        int listSize = fluidWidgets.size();
        if (!containerFluidStorage.getFluidStackList().isEmpty()) {
            outerLoop:
            for (int y = 0; y < 4; y++) {
                for (int x = 0; x < 9; x++) {
                    int widgetIndex = y * 9 + x + currentScroll * 9;
                    if (0 <= widgetIndex && widgetIndex < listSize) {
                        AbstractFluidWidget widget = fluidWidgets.get(widgetIndex);
                        widget.drawWidget(x * 18 + 7, y * 18 - 1);
                    } else {
                        break outerLoop;
                    }
                }
            }

            for (int x = 0; x < 9; x++) {
                for (int y = 0; y < 4; y++) {
                    int widgetIndex = y * 9 + x;
                    if (0 <= widgetIndex && widgetIndex < listSize) {
                        fluidWidgets.get(widgetIndex).drawTooltip(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
                    } else {
                        break;
                    }
                }
            }

            int deltaWheel = Mouse.getDWheel();
            if (deltaWheel > 0) {
                currentScroll++;
            } else if (deltaWheel < 0) {
                currentScroll--;
            }

            if (currentScroll < 0)
                currentScroll = 0;
            if (listSize / 9 < 4 && currentScroll < listSize / 9 + 4)
                currentScroll = 0;
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
        super.mouseClicked(mouseX, mouseY, mouseBtn);
        searchbar.mouseClicked(mouseX, mouseY, mouseBtn);
        int listSize = fluidWidgets.size();
        for (int x = 0; x < 9; x++) {
            for (int y = 0; y < 4; y++) {
                int index = y * 9 + x;
                if (0 <= index && index < listSize) {
                    AbstractFluidWidget widget = fluidWidgets.get(index);
                    widget.mouseClicked(x * 18 + 7, y * 18 - 1, mouseX, mouseY);
                }
            }
        }
    }

    @Override
    protected void keyTyped(char key, int keyID) {
        if (keyID == Keyboard.KEY_ESCAPE)
            mc.thePlayer.closeScreen();
        searchbar.textboxKeyTyped(key, keyID);
    }

    public int guiLeft() {
        return guiLeft;
    }

    public int guiTop() {
        return guiTop;
    }

    @Override
    public IFluidSelectorContainer getContainer() {
        return containerFluidStorage;
    }

    @Override
    public IAEFluidStack getCurrentFluid() {
        return containerFluidStorage.getSelectedFluidStack();
    }
}
