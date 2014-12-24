package extracells.gridblock;

import appeng.api.networking.*;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.parts.PartItemStack;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import extracells.api.IECTileEntity;
import extracells.part.PartECBase;
import extracells.registries.BlockEnum;
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
        return true;
    }

    @Override
    public final DimensionalCoord getLocation() {
        return host.getLocation();
    }

    @Override
    public final AEColor getGridColor() {
        return AEColor.Transparent;
    }

    @Override
    public void onGridNotification(GridNotification notification) {
    }

    @Override
    public final void setNetworkStatus(IGrid _grid, int _usedChannels) {
    	if(grid != null && host instanceof IListenerTile && grid != _grid){
    		((IListenerTile)host).updateGrid(grid, _grid);
    		grid = _grid;
    		usedChannels = _usedChannels;
    		if(host instanceof TileEntityFluidFiller && grid.getCache(IStorageGrid.class) != null)
    			((TileEntityFluidFiller) host).postChange(((IStorageGrid)grid.getCache(IStorageGrid.class)).getFluidInventory(), null, null);
    	}else{
    		grid = _grid;
    		usedChannels = _usedChannels;
    	}
    }

    @Override
    public final EnumSet<ForgeDirection> getConnectableSides() {
    	return EnumSet.of(ForgeDirection.DOWN,ForgeDirection.UP,ForgeDirection.NORTH,ForgeDirection.EAST,ForgeDirection.SOUTH,ForgeDirection.WEST);
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
    	DimensionalCoord loc = getLocation();
    	if(loc == null)
    		return null;
        return new ItemStack(loc.getWorld().getBlock(loc.x, loc.y, loc.z), 1, loc.getWorld().getBlockMetadata(loc.x, loc.y, loc.z));
    }
}