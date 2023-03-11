package extracells.part;

import static net.minecraftforge.common.util.Constants.NBT.TAG_COMPOUND;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartDeprecated;
import appeng.api.parts.IPartHost;
import appeng.api.parts.IPartRenderHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.container.ContainerBusFluidIO;
import extracells.gui.GuiBusFluidIO;
import extracells.integration.Integration;
import extracells.integration.ae2fc.FluidCraft;
import extracells.item.ItemPartECBase;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketBusFluidIO;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;

public abstract class PartFluidIO extends PartECBase
        implements IGridTickable, IInventoryUpdateReceiver, IFluidSlotPartOrBlock, IPartDeprecated {

    public Fluid[] filterFluids = new Fluid[9];
    private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
    protected byte filterSize;
    protected byte speedState;
    protected boolean redstoneControlled;
    private boolean lastRedstone;
    private final ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 4, 1, this) {

        @Override
        public boolean isItemValidForSlot(int i, ItemStack itemStack) {
            if (itemStack == null) return false;
            if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(itemStack)) return true;
            else if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(itemStack)) return true;
            else return AEApi.instance().definitions().materials().cardRedstone().isSameAs(itemStack);
        }
    };

    @Override
    public void getDrops(List<ItemStack> drops, boolean wrenched) {
        for (ItemStack stack : drops) {
            if (stack.getItem().getClass() == ItemPartECBase.class) {
                stack.stackTagCompound = null;
            }
        }

        for (ItemStack stack : upgradeInventory.slots) {
            if (stack == null) continue;
            drops.add(stack);
        }
    }

    @Override
    public int cableConnectionRenderTo() {
        return 5;
    }

    private boolean canDoWork() {
        boolean redstonePowered = isRedstonePowered();
        if (!this.redstoneControlled) return true;
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

    public byte getSpeedState() {
        return this.speedState;
    }

    public abstract TickRateModulation doWork(int rate, int TicksSinceLastCall);

    @Override
    public abstract void getBoxes(IPartCollisionHelper bch);

    @Override
    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiBusFluidIO(this, player);
    }

    public RedstoneMode getRedstoneMode() {
        return this.redstoneMode;
    }

    @Override
    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerBusFluidIO(this, player);
    }

    @Override
    public final TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(5, 60, false, false);
    }

    public ECPrivateInventory getUpgradeInventory() {
        return this.upgradeInventory;
    }

    @Override
    public List<String> getWailaBodey(NBTTagCompound tag, List<String> oldList) {
        if (tag.hasKey("speed")) oldList.add(tag.getInteger("speed") + "mB/t");
        else oldList.add(String.format("%smB/t", Extracells.basePartSpeed()));
        return oldList;
    }

    @Override
    public NBTTagCompound getWailaTag(NBTTagCompound tag) {
        tag.setInteger("speed", Extracells.basePartSpeed() + this.speedState * Extracells.basePartSpeed());
        return tag;
    }

    public void loopRedstoneMode(EntityPlayer player) {
        if (this.redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
            this.redstoneMode = RedstoneMode.values()[this.redstoneMode.ordinal() + 1];
        else this.redstoneMode = RedstoneMode.values()[0];
        new PacketBusFluidIO(this.redstoneMode).sendPacketToPlayer(player);
        saveData();
    }

    @Override
    public boolean onActivate(EntityPlayer player, Vec3 pos) {
        boolean activate = super.onActivate(player, pos);
        onInventoryChanged();
        return activate;
    }

    @Override
    public void onInventoryChanged() {
        this.filterSize = 0;
        this.redstoneControlled = false;
        this.speedState = 0;
        for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
            ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
            if (currentStack != null) {
                if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) this.filterSize++;
                if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(currentStack))
                    this.redstoneControlled = true;
                if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) this.speedState++;
            }
        }

        try {
            if (getHost().getLocation().getWorld().isRemote) return;
        } catch (Throwable ignored) {}
        new PacketBusFluidIO(this.filterSize).sendPacketToAllPlayers();
        new PacketBusFluidIO(this.redstoneControlled).sendPacketToAllPlayers();
        saveData();
    }

    @Override
    public void onNeighborChanged() {
        super.onNeighborChanged();
        boolean redstonePowered = isRedstonePowered();
        this.lastRedstone = redstonePowered;
    }

    /**
     * Map for converting slots from bus EC2 -> AE2FC
     */
    private static int[] FILTER_MAP = null;
    static {
        if (Integration.Mods.FLUIDCRAFT.isEnabled()) {
            FILTER_MAP = new int[] { 5, 3, 6, 1, 0, 2, 7, 4, 8 };
        }
    }

    @Override
    public final void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
        for (int i = 0; i < 9; i++) {
            this.filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
        }
        this.upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));
        onInventoryChanged();
    }

    @Override
    public NBTTagCompound transformNBT(NBTTagCompound extra) {
        if (Integration.Mods.FLUIDCRAFT.isEnabled()) {
            // Transform NBT in place
            // Fluid Filter
            NBTTagCompound fluidFilterNew = new NBTTagCompound();
            for (int slot = 0; slot < 9; ++slot) {
                String oldFilterName = "FilterFluid#" + slot;
                fluidFilterNew
                        .setTag("#" + FILTER_MAP[slot], FluidCraft.createFluidDisplay(extra.getString(oldFilterName)));
                extra.removeTag(oldFilterName);
            }
            extra.setTag("config", fluidFilterNew);
            // Upgrades
            NBTTagList upgrades = extra.getTagList("upgradeInventory", TAG_COMPOUND);
            NBTTagCompound upgradesNew = new NBTTagCompound();
            for (int i = 0; i < 4; ++i) {
                NBTTagCompound upgrade = upgrades.getCompoundTagAt(i);
                upgrade.removeTag("Slot");
                upgradesNew.setTag("#" + i, upgrade);
            }
            extra.removeTag("upgradeInventory");
            extra.setTag("upgrades", upgradesNew);
            // Redstone mode
            RedstoneMode redstoneMode = RedstoneMode.values()[extra.getInteger("redstoneMode")];
            switch (redstoneMode) {
                case LOW_SIGNAL:
                    extra.setString("REDSTONE_CONTROLLED", "LOW_SIGNAL");
                    break;
                case HIGH_SIGNAL:
                    extra.setString("REDSTONE_CONTROLLED", "HIGH_SIGNAL");
                    break;
                case SIGNAL_PULSE:
                    extra.setString("REDSTONE_CONTROLLED", "SIGNAL_PULSE");
                    break;
                default:
                    extra.setString("REDSTONE_CONTROLLED", "IGNORE");
                    break;
            }
            extra.removeTag("redstoneMode");
            // Part data
            extra.setTag("part", extra.getCompoundTag("node").getCompoundTag("node0"));
            extra.removeTag("node");
            // Extra tags
            extra.setString("CRAFT_ONLY", "NO");
            extra.setString("FUZZY_MODE", "IGNORE_ALL");
            extra.setString("SCHEDULING_MODE", "DEFAULT");
        }
        return extra;
    }

    @Override
    public final boolean readFromStream(ByteBuf data) throws IOException {
        return super.readFromStream(data);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public final void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer) {}

    @SideOnly(Side.CLIENT)
    @Override
    public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

    @SideOnly(Side.CLIENT)
    @Override
    public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

    public void sendInformation(EntityPlayer player) {
        new PacketFluidSlot(Arrays.asList(this.filterFluids)).sendPacketToPlayer(player);
        new PacketBusFluidIO(this.redstoneMode).sendPacketToPlayer(player);
        new PacketBusFluidIO(this.filterSize).sendPacketToPlayer(player);
    }

    @Override
    public final void setFluid(int index, Fluid fluid, EntityPlayer player) {
        this.filterFluids[index] = fluid;
        new PacketFluidSlot(Arrays.asList(this.filterFluids)).sendPacketToPlayer(player);
        saveData();
    }

    @Override
    public void setPartHostInfo(ForgeDirection _side, IPartHost _host, TileEntity _tile) {
        super.setPartHostInfo(_side, _host, _tile);
        onInventoryChanged();
    }

    @Override
    public final TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall) {
        return canDoWork()
                ? doWork(Extracells.basePartSpeed() + this.speedState * Extracells.basePartSpeed(), TicksSinceLastCall)
                : TickRateModulation.IDLE;
    }

    @Override
    public final void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("redstoneMode", this.redstoneMode.ordinal());
        for (int i = 0; i < this.filterFluids.length; i++) {
            Fluid fluid = this.filterFluids[i];
            if (fluid != null) data.setString("FilterFluid#" + i, fluid.getName());
            else data.setString("FilterFluid#" + i, "");
        }
        data.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
    }

    @Override
    public final void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
    }
}
