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
        host = _host;
    }

    @Override
    public double getIdlePowerUsage() {
        return host.getPowerUsage();
    }

    @Override
    public EnumSet<GridFlags> getFlags() {
        return EnumSet.of(GridFlags.REQUIRE_CHANNEL);
    }

    @Override
    public final boolean isWorldAccessible() {
        return false;
    }

    @Override
    public final DimensionalCoord getLocation() {
        return host.getLocation();
    }

    @Override
    public final AEColor getGridColor() {
        return color;
    }

    @Override
    public void onGridNotification(GridNotification notification) {
    }

    @Override
    public final void setNetworkStatus(IGrid _grid, int _usedChannels) {
        grid = _grid;
        usedChannels = _usedChannels;
    }

    @Override
    public final EnumSet<ForgeDirection> getConnectableSides() {
        return null;
    }

    @Override
    public IGridHost getMachine() {
        return host;
    }

    @Override
    public void gridChanged() {
    }

    @Override
    public ItemStack getMachineRepresentation() {
        return host.getItemStack(PartItemStack.Network);
    }

    public IMEMonitor<IAEFluidStack> getFluidMonitor() {
        IGridNode node = host.getGridNode();
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
}