package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerBusFluidIO;
import extracells.gui.GuiBusFluidIO;
import extracells.network.packet.other.IFluidSlotPart;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketBusFluidIO;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;
import java.util.Arrays;

public abstract class PartFluidIO extends PartECBase implements IGridTickable, IInventoryUpdateReceiver, IFluidSlotPart {

    protected Fluid[] filterFluids = new Fluid[9];
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    protected byte filterSize;
    protected byte speedState;
    protected boolean redstoneControlled;
    private boolean lastRedstone;
    private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 4, 1, this) {

        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            if (itemStack == null)
                return false;
            if (AEApi.instance().materials().materialCardCapacity.sameAs(itemStack))
                return true;
            else if (AEApi.instance().materials().materialCardSpeed.sameAs(itemStack))
                return true;
            else if (AEApi.instance().materials().materialCardRedstone.sameAs(itemStack))
                return true;
            return false;
        }
    };

    @SideOnly(Side.CLIENT)
    @Override
    public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

    @SideOnly(Side.CLIENT)
    @Override
    public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

    @SideOnly(Side.CLIENT)
    @Override
    public final void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer) {
    }

    public ECPrivateInventory getUpgradeInventory() {
        return upgradeInventory;
    }

    @Override
    public void setPartHostInfo(ForgeDirection _side, IPartHost _host, TileEntity _tile) {
        super.setPartHostInfo(_side, _host, _tile);
        onInventoryChanged();
    }

    @Override
    public final void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("redstoneMode", redstoneMode.ordinal());
        for (int i = 0; i < filterFluids.length; i++) {
            Fluid fluid = filterFluids[i];
            if (fluid != null)
                data.setString("FilterFluid#" + i, fluid.getName());
            else
                data.setString("FilterFluid#" + i, "");
        }
        data.setTag("upgradeInventory", upgradeInventory.writeToNBT());
    }

    @Override
    public int getLightLevel() {
        return 0;
    }

    @Override
    public final void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
        for (int i = 0; i < 9; i++) {
            filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
        }
        upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));
        onInventoryChanged();
    }

    @Override
    public final void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
    }

    @Override
    public final boolean readFromStream(ByteBuf data) throws IOException {
        return super.readFromStream(data);
    }

    @Override
    public abstract void getBoxes(IPartCollsionHelper bch);

    @Override
    public int cableConnectionRenderTo() {
        return 5;
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        if (canDoWork())
            return doWork(125 + speedState * 125, TicksSinceLastCall) ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
        return TickRateModulation.SLOWER;
    }

    public abstract boolean doWork(int rate, int TicksSinceLastCall);

    public final void setFluid(int index, Fluid fluid, EntityPlayer player) {
        filterFluids[index] = fluid;
        new PacketFluidSlot(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
    }

    public RedstoneMode getRedstoneMode() {
        return redstoneMode;
    }

    public void loopRedstoneMode(EntityPlayer player) {
        if (redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
            redstoneMode = RedstoneMode.values()[redstoneMode.ordinal() + 1];
        else
            redstoneMode = RedstoneMode.values()[0];
        new PacketBusFluidIO(redstoneMode).sendPacketToPlayer(player);
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerBusFluidIO(this, player);
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiBusFluidIO(this, player);
    }

    public void sendInformation(EntityPlayer player) {
        new PacketFluidSlot(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
        new PacketBusFluidIO(redstoneMode).sendPacketToPlayer(player);
        new PacketBusFluidIO(filterSize).sendPacketToPlayer(player);
    }

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        boolean activate = super.onActivate(player, pos);
        onInventoryChanged();
        return activate;
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();

        if (redstonePowered) {
            if (!lastRedstone) {
                doWork(125 + speedState + speedState * 125, 1);
            } else {
                lastRedstone = true;
                doWork(125 + speedState + speedState * 125, 1);
            }
        }
        lastRedstone = redstonePowered;
    }

    @Override
    public void onInventoryChanged() {
        filterSize = 0;
        redstoneControlled = false;
        speedState = 0;
        for (int i = 0; i < upgradeInventory.getSizeInventory(); i++) {
            ItemStack currentStack = upgradeInventory.getStackInSlot(i);
            if (currentStack != null) {
                if (AEApi.instance().materials().materialCardCapacity.sameAs(currentStack))
                    filterSize++;
                if (AEApi.instance().materials().materialCardRedstone.sameAs(currentStack))
                    redstoneControlled = true;
                if (AEApi.instance().materials().materialCardSpeed.sameAs(currentStack))
                    speedState++;
            }
        }

        try {
            if (host.getLocation().getWorld().isRemote)
                return;
        } catch (Throwable ignored) {
        }
        new PacketBusFluidIO(filterSize).sendPacketToAllPlayers();
        new PacketBusFluidIO(redstoneControlled).sendPacketToAllPlayers();
    }

    private boolean canDoWork() {
        if (!redstoneControlled)
            return true;
        switch (getRedstoneMode()) {
            case IGNORE:
                return true;
            case LOW_SIGNAL:
                return !redstonePowered;
            case HIGH_SIGNAL:
                return redstonePowered;
            case SIGNAL_PULSE:
                return false;
        }
        return false;
    }
}
