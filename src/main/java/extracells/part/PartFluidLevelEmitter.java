package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.container.ContainerFluidEmitter;
import extracells.gui.GuiFluidEmitter;
import extracells.network.packet.other.IFluidSlotPart;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PartFluidLevelEmitter extends PartECBase implements IStackWatcherHost, IFluidSlotPart {

    private Fluid fluid;
    private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
    private IStackWatcher watcher;
    private long wantedAmount;
    private long currentAmount;

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
        rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[0]);
        rh.setBounds(7, 7, 11, 9, 9, 14);
        rh.renderInventoryBox(renderer);

        rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[1]);
        rh.setBounds(7, 7, 14, 9, 9, 16);
        rh.renderInventoryBox(renderer);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer) {
        rh.setTexture(TextureManager.LEVEL_FRONT.getTextures()[0]);
        rh.setBounds(7, 7, 11, 9, 9, 14);
        rh.renderBlock(x, y, z, renderer);

        rh.setTexture(isPowering() ? TextureManager.LEVEL_FRONT.getTextures()[2] : TextureManager.LEVEL_FRONT.getTextures()[1]);
        rh.setBounds(7, 7, 14, 9, 9, 16);
        rh.renderBlock(x, y, z, renderer);
    }

    @Override
    public void writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        if (fluid != null)
            data.setString("fluid", fluid.getName());
        else
            data.removeTag("fluid");
        data.setInteger("mode", mode.ordinal());
        data.setLong("wantedAmount", wantedAmount);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        fluid = FluidRegistry.getFluid(data.getString("fluid"));
        mode = RedstoneMode.values()[data.getInteger("mode")];
        wantedAmount = data.getLong("wantedAmount");
    }

    @Override
    public void getBoxes(IPartCollsionHelper bch) {
        bch.addBox(7, 7, 11, 9, 9, 16);
    }

    @Override
    public int cableConnectionRenderTo() {
        return 8;
    }

    @Override
    public void updateWatcher(IStackWatcher newWatcher) {
        watcher = newWatcher;
        if (fluid != null)
            watcher.add(AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1)));
    }

    @Override
    public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, BaseActionSource src, StorageChannel chan) {
        if (chan == StorageChannel.FLUIDS && diffStack != null && ((IAEFluidStack) diffStack).getFluid() == fluid) {
            currentAmount = fullStack != null ? fullStack.getStackSize() : 0;
            if (node != null) {
                isActive = node.isActive();
                host.markForUpdate();
                tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air);
                tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ, Blocks.air);
            }
        }
    }

    @Override
    public int isProvidingStrongPower() {
        return isPowering() ? 15 : 0;
    }

    @Override
    public int isProvidingWeakPower() {
        return isProvidingStrongPower();
    }

    private boolean isPowering() {
        switch (mode) {
            case LOW_SIGNAL:
                return wantedAmount >= currentAmount;
            case HIGH_SIGNAL:
                return wantedAmount <= currentAmount;
            default:
                return false;
        }
    }

    @Override
    public void setFluid(int _index, Fluid _fluid, EntityPlayer _player) {
        fluid = _fluid;
        if (watcher == null)
            return;
        watcher.clear();
        updateWatcher(watcher);
        new PacketFluidSlot(Lists.newArrayList(fluid)).sendPacketToPlayer(_player);
    }

    public void toggleMode(EntityPlayer player) {
        switch (mode) {
            case LOW_SIGNAL:
                mode = RedstoneMode.HIGH_SIGNAL;
                break;
            default:
                mode = RedstoneMode.LOW_SIGNAL;
                break;
        }

        tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, Blocks.air);
        tile.getWorldObj().notifyBlocksOfNeighborChange(tile.xCoord + side.offsetX, tile.yCoord + side.offsetY, tile.zCoord + side.offsetZ, Blocks.air);
        new PacketFluidEmitter(mode, player).sendPacketToPlayer(player);
    }

    public void setWantedAmount(long _wantedAmount, EntityPlayer player) {
        wantedAmount = _wantedAmount;
        if (wantedAmount < 0)
            wantedAmount = 0;
        new PacketFluidEmitter(wantedAmount, player).sendPacketToPlayer(player);
    }

    public void changeWantedAmount(int modifier, EntityPlayer player) {
        setWantedAmount(wantedAmount + modifier, player);
    }

    public void syncClientGui(EntityPlayer player) {
        new PacketFluidEmitter(mode, player).sendPacketToPlayer(player);
        new PacketFluidEmitter(wantedAmount, player).sendPacketToPlayer(player);
        new PacketFluidSlot(Lists.newArrayList(fluid)).sendPacketToPlayer(player);
    }

    public Object getServerGuiElement(EntityPlayer player) {
        return new ContainerFluidEmitter(this, player);
    }

    public Object getClientGuiElement(EntityPlayer player) {
        return new GuiFluidEmitter(this, player);
    }
}
