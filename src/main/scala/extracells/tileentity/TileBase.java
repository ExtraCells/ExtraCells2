package extracells.tileentity;

import javax.annotation.Nullable;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.data.IAEStack;
import extracells.util.StorageChannels;
import net.minecraft.block.state.IBlockState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.util.AEPartLocation;

public abstract class TileBase extends TileEntity {
	@Nullable
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		handleUpdateTag(pkt.getNbtCompound());
	}

	public void updateBlock() {
		if (world == null || pos == null) {
			return;
		}
		IBlockState blockState = world.getBlockState(pos);
		world.notifyBlockUpdate(pos, blockState, blockState, 0);
	}

	public IStorageGrid getStorageGrid(AEPartLocation side) {
		if (!(this instanceof IGridHost)) {
			return null;
		}
		IGridHost host = (IGridHost) this;
		if (host.getGridNode(side) == null) {
			return null;
		}
		IGrid grid = host.getGridNode(side).getGrid();
		if (grid == null) {
			return null;
		}
		return grid.getCache(IStorageGrid.class);
	}

	@Nullable
	public IMEMonitor<IAEFluidStack> getFluidInventory(AEPartLocation side) {
		IStorageGrid storageGrid = getStorageGrid(side);
		if (storageGrid == null) {
			return null;
		} else {
			return storageGrid.getInventory(StorageChannels.FLUID());
		}
	}

	@Nullable
	public IMEMonitor<IAEItemStack> getItemInventory(AEPartLocation side) {
		IStorageGrid storageGrid = getStorageGrid(side);
		if (storageGrid == null) {
			return null;
		} else {
			return storageGrid.getInventory(StorageChannels.ITEM());
		}
	}

	@Nullable
	public <T extends IAEStack<T>> IMEMonitor<T>  getInventory(AEPartLocation side, IStorageChannel<T> channel) {
		IStorageGrid storageGrid = getStorageGrid(side);
		if (storageGrid == null) {
			return null;
		} else {
			return storageGrid.getInventory(channel);
		}
	}
}
