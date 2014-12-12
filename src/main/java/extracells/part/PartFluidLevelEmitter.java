package extracells.part;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Random;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
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
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketFluidEmitter;
import extracells.render.TextureManager;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class PartFluidLevelEmitter extends PartECBase implements IStackWatcherHost, IFluidSlotPartOrBlock {

    private Fluid fluid;
    private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
    private IStackWatcher watcher;
    private long wantedAmount;
    private long currentAmount;
    private boolean clientRedstoneOutput = false;

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

        rh.setTexture(clientRedstoneOutput ? TextureManager.LEVEL_FRONT.getTextures()[2] : TextureManager.LEVEL_FRONT.getTextures()[1]);
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
        if (wantedAmount < 0)
        	wantedAmount = 0;
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
            IGridNode node = getGridNode();
            if (node != null) {
                setActive(node.isActive());
                getHost().markForUpdate();
                notifyBlocky(getHostTile(), getSide());
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
        saveData();
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

        notifyBlocky(getHostTile(), getSide());
        new PacketFluidEmitter(mode, player).sendPacketToPlayer(player);
        saveData();
    }

    public void setWantedAmount(long _wantedAmount, EntityPlayer player) {
        wantedAmount = _wantedAmount;
        if (wantedAmount < 0)
        	wantedAmount = 0;
        new PacketFluidEmitter(wantedAmount, player).sendPacketToPlayer(player);
        notifyBlocky(getHostTile(), getSide());
        saveData();
    }

    private void notifyBlocky(TileEntity _tile, ForgeDirection _side) {
        _tile.getWorldObj().notifyBlocksOfNeighborChange(_tile.xCoord, _tile.yCoord, _tile.zCoord, Blocks.air);
        _tile.getWorldObj().notifyBlocksOfNeighborChange(_tile.xCoord + _side.offsetX, _tile.yCoord + _side.offsetY, _tile.zCoord + _side.offsetZ, Blocks.air);
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
    
    @Override
    public void writeToStream(ByteBuf data) throws IOException {
        super.writeToStream(data);
        data.writeBoolean(isPowering());
    }

    @Override
    public boolean readFromStream(ByteBuf data) throws IOException {
    	super.readFromStream(data);
    	clientRedstoneOutput = data.readBoolean();
        return true;
    }
    
    @Override
	public void randomDisplayTick(World world, int x, int y, int z, Random r){
		if (clientRedstoneOutput){
			ForgeDirection d = getSide();
			double d0 = d.offsetX * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d1 = d.offsetY * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			double d2 = d.offsetZ * 0.45F + (r.nextFloat() - 0.5F) * 0.2D;
			world.spawnParticle( "reddust", 0.5 + x + d0, 0.5 + y + d1, 0.5 + z + d2, 0.0D, 0.0D, 0.0D );
		}
	}
    
    @Override
    public double getPowerUsage() {
    	return 1.0D;
    }
    
}
