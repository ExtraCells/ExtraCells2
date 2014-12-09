package extracells.gui;

import appeng.api.AEApi;
import extracells.container.ContainerBusFluidStorage;
import extracells.gui.widget.WidgetRedstoneModes;
import extracells.gui.widget.fluid.WidgetFluidSlot;
import extracells.network.packet.other.IFluidSlotGui;
import extracells.network.packet.part.PacketBusFluidStorage;
import extracells.part.PartFluidStorage;
import extracells.util.FluidUtil;
import extracells.util.GuiUtil;
import net.minecraft.client.Minecraft;
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

public class GuiBusFluidStorage extends GuiContainer implements WidgetFluidSlot.IConfigurable, IFluidSlotGui {

    private static final ResourceLocation guiTexture = new ResourceLocation("extracells", "textures/gui/storagebusfluid.png");
    private EntityPlayer player;
    private byte filterSize;
    private List<WidgetFluidSlot> fluidSlotList = new ArrayList<WidgetFluidSlot>();
    private boolean hasNetworkTool;

    public GuiBusFluidStorage(PartFluidStorage part, EntityPlayer _player) {
        super(new ContainerBusFluidStorage(part, _player));
        ((ContainerBusFluidStorage) inventorySlots).setGui(this);
        player = _player;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 6; j++) {
                fluidSlotList.add(new WidgetFluidSlot(player, part, i * 6 + j, 18 * i + 7, 18 * j + 17));
            }
        }

        new PacketBusFluidStorage(player, part).sendPacketToServer();
        hasNetworkTool = inventorySlots.getInventory().size() > 40;
        xSize = hasNetworkTool ? 246 : 211;
        ySize = 222;

    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float alpha, int mouseX, int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(guiTexture);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, 176, 222);
        drawTexturedModalRect(guiLeft + 179, guiTop, 179, 0, 32, 86);
        if (hasNetworkTool)
            drawTexturedModalRect(guiLeft + 179, guiTop + 93, 178, 93, 68, 68);

    }

    public void shiftClick(ItemStack itemStack) {
        FluidStack containerFluid = FluidUtil.getFluidFromContainer(itemStack);
        Fluid fluid = containerFluid == null ? null : containerFluid.getFluid();
        for (WidgetFluidSlot fluidSlot : fluidSlotList) {
            if (fluidSlot.getFluid() == null || (fluid != null && fluidSlot.getFluid() == fluid)) {
                fluidSlot.mouseClicked(itemStack);
                return;
            }
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(mouseX, mouseY);
        boolean overlayRendered = false;
        for (byte i = 0; i < 54; i++) {
            fluidSlotList.get(i).drawWidget();
            if (!overlayRendered && fluidSlotList.get(i).canRender())
                overlayRendered = GuiUtil.renderOverlay(zLevel, guiLeft, guiTop, fluidSlotList.get(i), mouseX, mouseY);
        }

        for (Object button : buttonList) {
            if (button instanceof WidgetRedstoneModes)
                ((WidgetRedstoneModes) button).drawTooltip(mouseX, mouseY, (this.width - xSize) / 2, (this.height - ySize) / 2);
        }
    }

    public void changeConfig(byte _filterSize) {
        filterSize = _filterSize;
    }

    public void updateFluids(List<Fluid> fluidList) {
        for (int i = 0; i < fluidSlotList.size() && i < fluidList.size(); i++) {
            fluidSlotList.get(i).setFluid(fluidList.get(i));
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseBtn) {
        Slot slot = getSlotAtPosition(mouseX, mouseY);

        if (slot != null && slot.getStack() != null && slot.getStack().isItemEqual(AEApi.instance().items().itemNetworkTool.stack(1)))
            return;
        super.mouseClicked(mouseX, mouseY, mouseBtn);
        for (WidgetFluidSlot fluidSlot : fluidSlotList) {
            if (GuiUtil.isPointInRegion(guiLeft, guiTop, fluidSlot.getPosX(), fluidSlot.getPosY(), 18, 18, mouseX, mouseY)) {
                fluidSlot.mouseClicked(player.inventory.getItemStack());
                break;
            }
        }
    }

    @Override
    public byte getConfigState() {
        return filterSize;
    }

    protected Slot getSlotAtPosition(int p_146975_1_, int p_146975_2_) {
        for (int k = 0; k < inventorySlots.inventorySlots.size(); ++k) {
            Slot slot = (Slot) inventorySlots.inventorySlots.get(k);

            if (this.isMouseOverSlot(slot, p_146975_1_, p_146975_2_)) {
                return slot;
            }
        }

        return null;
    }

    private boolean isMouseOverSlot(Slot p_146981_1_, int p_146981_2_, int p_146981_3_) {
        return this.func_146978_c(p_146981_1_.xDisplayPosition, p_146981_1_.yDisplayPosition, 16, 16, p_146981_2_, p_146981_3_);
    }
}
