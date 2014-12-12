package extracells.part;

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidTerminal;
import extracells.gridblock.ECBaseGridBlock;
import extracells.gui.GuiFluidTerminal;
import extracells.network.packet.part.PacketFluidTerminal;
import extracells.render.TextureManager;
import extracells.util.FluidUtil;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.apache.commons.lang3.tuple.MutablePair;

import java.util.ArrayList;
import java.util.List;

public class PartFluidTerminal extends PartECBase implements IGridTickable, IInventoryUpdateReceiver {

    private Fluid currentFluid;
    private List<ContainerFluidTerminal> containers = new ArrayList<ContainerFluidTerminal>();
    private ECPrivateInventory inventory = new ECPrivateInventory("extracells.part.fluid.terminal", 2, 64, this) {

        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            return FluidUtil.isFluidContainer(itemStack);
        }
    };
    private MachineSource machineSource = new MachineSource(this);

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
        rh.setTexture(side);
        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderInventoryBox(renderer);
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderInventoryBox(renderer);

        ts.setBrightness(13 << 20 | 13 << 4);

        rh.setInvColor(0xFFFFFF);
        rh.renderInventoryFace(TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

        rh.setBounds(3, 3, 15, 13, 13, 16);
        rh.setInvColor(AEColor.Transparent.blackVariant);
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.mediumVariant);
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
        rh.setInvColor(AEColor.Transparent.whiteVariant);
        rh.renderInventoryFace(TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        renderInventoryBusLights(rh, renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        Tessellator ts = Tessellator.instance;

        IIcon side = TextureManager.TERMINAL_SIDE.getTexture();
        rh.setTexture(side);
        rh.setBounds(4, 4, 13, 12, 12, 14);
        rh.renderBlock(x, y, z, renderer);
        rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(), side, side);
        rh.setBounds(2, 2, 14, 14, 14, 16);
        rh.renderBlock(x, y, z, renderer);

        if (isActive())
            Tessellator.instance.setBrightness(13 << 20 | 13 << 4);

        ts.setColorOpaque_I(0xFFFFFF);
        rh.renderFace(x, y, z, TextureManager.BUS_BORDER.getTexture(), ForgeDirection.SOUTH, renderer);

        IPartHost host = getHost();
        rh.setBounds(3, 3, 15, 13, 13, 16);
        ts.setColorOpaque_I(host.getColor().blackVariant);
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[0], ForgeDirection.SOUTH, renderer);
        ts.setColorOpaque_I(host.getColor().mediumVariant);
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[1], ForgeDirection.SOUTH, renderer);
        ts.setColorOpaque_I(host.getColor().whiteVariant);
        rh.renderFace(x, y, z, TextureManager.TERMINAL_FRONT.getTextures()[2], ForgeDirection.SOUTH, renderer);

        rh.setBounds(5, 5, 12, 11, 11, 13);
        renderStaticBusLights(x, y, z, rh, renderer);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("inventory", inventory.writeToNBT());
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        inventory.readFromNBT(data.getTagList("inventory", 10));
    }

    @Override
    public void getBoxes(IPartCollsionHelper bch) {
        bch.addBox(2, 2, 14, 14, 14, 16);
        bch.addBox(4, 4, 13, 12, 12, 14);
        bch.addBox(5, 5, 12, 11, 11, 13);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 1;
    }

    public void setCurrentFluid(Fluid _currentFluid) {
        currentFluid = _currentFluid;
        sendCurrentFluid();
    }

    public void sendCurrentFluid() {
        for (ContainerFluidTerminal containerFluidTerminal : containers) {
            sendCurrentFluid(containerFluidTerminal);
        }
    }

    public void sendCurrentFluid(ContainerFluidTerminal containerFluidTerminal) {
        new PacketFluidTerminal(containerFluidTerminal.getPlayer(), currentFluid).sendPacketToPlayer(containerFluidTerminal.getPlayer());
    }

    public void addContainer(ContainerFluidTerminal containerTerminalFluid) {
        containers.add(containerTerminalFluid);
        sendCurrentFluid();
    }

    public void removeContainer(ContainerFluidTerminal containerTerminalFluid) {
        containers.remove(containerTerminalFluid);
    }

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        if (isActive())
            return super.onActivate(player, pos);
        return false;
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerFluidTerminal(this, player);
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiFluidTerminal(this, player);
    }

    public IInventory getInventory() {
        return inventory;
    }

    public void doWork() {
        ItemStack secondSlot = inventory.getStackInSlot(1);
        if (secondSlot != null && secondSlot.stackSize >= secondSlot.getMaxStackSize())
            return;
        ItemStack container = inventory.getStackInSlot(0);
        if (!FluidUtil.isFluidContainer(container))
            return;
        container = container.copy();
        container.stackSize = 1;

        ECBaseGridBlock gridBlock = getGridBlock();
        if (gridBlock == null)
            return;
        IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
        if (monitor == null)
            return;

        if (FluidUtil.isEmpty(container)) {
            if (currentFluid == null)
                return;
            int capacity = FluidUtil.getCapacity(container);
            IAEFluidStack result = monitor.extractItems(FluidUtil.createAEFluidStack(currentFluid, capacity), Actionable.SIMULATE, machineSource);
            int proposedAmount = result == null ? 0 : (int) Math.min(capacity, result.getStackSize());
            MutablePair<Integer, ItemStack> filledContainer = FluidUtil.fillStack(container, new FluidStack(currentFluid, proposedAmount));
            if (fillSecondSlot(filledContainer.getRight())) {
                monitor.extractItems(FluidUtil.createAEFluidStack(currentFluid, filledContainer.getLeft()), Actionable.MODULATE, machineSource);
                decreaseFirstSlot();
            }
        } else  {
            FluidStack containerFluid = FluidUtil.getFluidFromContainer(container);
            IAEFluidStack notInjected = monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.SIMULATE, machineSource);
            if (notInjected != null)
                return;
            MutablePair<Integer, ItemStack> drainedContainer = FluidUtil.drainStack(container, containerFluid);
            ItemStack emptyContainer = drainedContainer.getRight();
            if (emptyContainer == null || fillSecondSlot(emptyContainer)) {
                monitor.injectItems(FluidUtil.createAEFluidStack(containerFluid), Actionable.MODULATE, machineSource);
                decreaseFirstSlot();
            }
        }
    }

    public boolean fillSecondSlot(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        ItemStack secondSlot = inventory.getStackInSlot(1);
        if (secondSlot == null) {
            inventory.setInventorySlotContents(1, itemStack);
            return true;
        } else {
            if (!secondSlot.isItemEqual(itemStack) || !ItemStack.areItemStackTagsEqual(itemStack, secondSlot))
                return false;
            inventory.incrStackSize(1, itemStack.stackSize);
            return true;
        }
    }

    public void decreaseFirstSlot() {
        ItemStack slot = inventory.getStackInSlot(0);
        slot.stackSize--;
        if (slot.stackSize <= 0)
            inventory.setInventorySlotContents(0, null);
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        doWork();
        return TickRateModulation.FASTER;
    }

    @Override
    public void onInventoryChanged() {
        saveData();
    }
    
    @Override
    public double getPowerUsage() {
    	return 0.5D;
    }
}
