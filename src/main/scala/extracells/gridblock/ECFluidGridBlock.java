package extracells.gridblock;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridNotification;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridHost;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.tileentity.IListenerTile;
import extracells.tileentity.TileEntityFluidFiller;

public class ECFluidGridBlock implements IGridBlock {

	protected final IECTileEntity host;
	protected IGrid grid;
	protected int usedChannels;

	public ECFluidGridBlock(IECTileEntity host) {
		this.host = host;
	}

	@Override
	public final EnumSet<EnumFacing> getConnectableSides() {
		return EnumSet.allOf(EnumFacing.class);
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public final AEColor getGridColor() {
		return AEColor.TRANSPARENT;
	}

	@Override
	public double getIdlePowerUsage() {
		return this.host.getPowerUsage();
	}

	@Override
	public final DimensionalCoord getLocation() {
		return this.host.getLocation();
	}

	@Override
	public IGridHost getMachine() {
		return this.host;
	}

	@Override
	public ItemStack getMachineRepresentation() {
		DimensionalCoord loc = getLocation();
		if (loc == null) {
			return null;
		}
		IBlockState blockState = loc.getWorld().getBlockState(loc.getPos());
		Block block = blockState.getBlock();
		return new ItemStack(block, 1, block.getMetaFromState(blockState));
	}

	@Override
	public void gridChanged() {
	}

	@Override
	public final boolean isWorldAccessible() {
		return true;
	}

	@Override
	public void onGridNotification(GridNotification notification) {
	}

	@Override
	public final void setNetworkStatus(IGrid grid, int usedChannels) {
		if (this.grid != null && this.host instanceof IListenerTile && this.grid != grid) {
			((IListenerTile) this.host).updateGrid(this.grid, grid);
			this.grid = grid;
			this.usedChannels = usedChannels;
			if (this.host instanceof TileEntityFluidFiller && this.grid.getCache(IStorageGrid.class) != null) {
				TileEntityFluidFiller fluidFiller = (TileEntityFluidFiller) host;
				IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
				fluidFiller.postChange(storageGrid.getFluidInventory(), null, null);
			}
		} else {
			this.grid = grid;
			this.usedChannels = usedChannels;
		}
	}
}
