package extracells.gridblock;

import appeng.api.networking.*;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import extracells.part.PartECBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.EnumSet;

public class ECBaseGridBlock implements IGridBlock {

	protected AEColor color;
	protected IGrid grid;
	protected int usedChannels;
	protected PartECBase host;

	public ECBaseGridBlock(PartECBase _host) {
		this.host = _host;
	}

	@Override
	public final EnumSet<ForgeDirection> getConnectableSides() {
		return EnumSet.noneOf(ForgeDirection.class);
	}

	@Override
	public EnumSet<GridFlags> getFlags() {
		return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
	}

	public IMEMonitor<IAEFluidStack> getFluidMonitor() {
		IGridNode node = this.host.getGridNode();
		if (node == null)
			return null;
		IGrid grid = node.getGrid();
		if (grid == null)
			return null;
		IStorageGrid storageGrid = grid.getCache(IStorageGrid.class);
		if (storageGrid == null)
			return null;
		return storageGrid.getFluidInventory();

	}

	@Override
	public final AEColor getGridColor() {
		return this.color == null ? AEColor.Transparent : this.color;
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
		return this.host.getItemStack(PartItemStack.Network);
	}

	@Override
	public void gridChanged() {}

	@Override
	public final boolean isWorldAccessible() {
		return false;
	}

	@Override
	public void onGridNotification(GridNotification notification) {}

	@Override
	public final void setNetworkStatus(IGrid _grid, int _usedChannels) {
		this.grid = _grid;
		this.usedChannels = _usedChannels;
	}
}
