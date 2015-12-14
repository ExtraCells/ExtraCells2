package extracells.gridblock;

import appeng.api.networking.*;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.tileentity.IListenerTile;
import extracells.tileentity.TileEntityFluidFiller;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

public class ECFluidGridBlock implements IGridBlock {

	protected IGrid grid;
	protected int usedChannels;
	protected IECTileEntity host;

	public ECFluidGridBlock(IECTileEntity _host) {
		this.host = _host;
	}

	@Override
	public final EnumSet<ForgeDirection> getConnectableSides() {
		return EnumSet.of(ForgeDirection.DOWN, ForgeDirection.UP,
				ForgeDirection.NORTH, ForgeDirection.EAST,
				ForgeDirection.SOUTH, ForgeDirection.WEST);
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
	}

	@Override
	public final AEColor getGridColor() {
		return AEColor.Transparent;
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
		if (loc == null)
			return null;
		return new ItemStack(loc.getWorld().getBlock(loc.x, loc.y, loc.z), 1,
				loc.getWorld().getBlockMetadata(loc.x, loc.y, loc.z));
	}

	@Override
	public void gridChanged() {}

	@Override
	public final boolean isWorldAccessible() {
		return true;
	}

	@Override
	public void onGridNotification(GridNotification notification) {}

	@Override
	public final void setNetworkStatus(IGrid _grid, int _usedChannels) {
		if (this.grid != null && this.host instanceof IListenerTile
				&& this.grid != _grid) {
			((IListenerTile) this.host).updateGrid(this.grid, _grid);
			this.grid = _grid;
			this.usedChannels = _usedChannels;
			if (this.host instanceof TileEntityFluidFiller
					&& this.grid.getCache(IStorageGrid.class) != null)
				((TileEntityFluidFiller) this.host).postChange(
						((IStorageGrid) this.grid.getCache(IStorageGrid.class))
								.getFluidInventory(), null, null);
		} else {
			this.grid = _grid;
			this.usedChannels = _usedChannels;
		}
	}
}
