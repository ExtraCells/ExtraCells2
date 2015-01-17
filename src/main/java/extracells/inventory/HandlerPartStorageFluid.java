package extracells.inventory;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.implementations.tiles.ITileStorageMonitorable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.part.PartFluidStorage;
import extracells.util.FluidUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerPartStorageFluid implements IMEInventoryHandler<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

    private PartFluidStorage node;
    private IFluidHandler tank;
    private AccessRestriction access;
    private List<Fluid> prioritizedFluids = new ArrayList<Fluid>();
    private boolean inverted;
    public ITileStorageMonitorable externalSystem;

    public HandlerPartStorageFluid(PartFluidStorage _node) {
        node = _node;
    }

    @Override
    public AccessRestriction getAccess() {
        return access;
    }

    @Override
    public boolean isPrioritized(IAEFluidStack input) {
        if (input == null)
            return false;
        for (Fluid fluid : prioritizedFluids)
            if (fluid == input.getFluid())
                return true;
        return false;
    }

    @Override
    public boolean canAccept(IAEFluidStack input) {
    	if(!node.isActive())
    		return false;
    	if ((tank == null && externalSystem == null) || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS || input == null)
            return false;
        if(externalSystem != null){
        	IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), new MachineSource(node));
    		if(monitor == null)
    			return false;
    		IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    		if(fluidInventory == null)
    			return false;
    		return fluidInventory.canAccept(input);
        }
        FluidTankInfo[] infoArray = tank.getTankInfo(node.getSide().getOpposite());
        if (infoArray != null && infoArray.length > 0) {
            FluidTankInfo info = infoArray[0];
            if (info.fluid == null || info.fluid.amount == 0 || info.fluid.fluidID == input.getFluidStack().fluidID)
                if (inverted)
                    return !prioritizedFluids.isEmpty() || !isPrioritized(input);
                else
                    return prioritizedFluids.isEmpty() || isPrioritized(input);
        }
        return false;
    }

    @Override
    public int getPriority() {
        return node.getPriority();
    }

    @Override
    public int getSlot() {
        return 0;
    }

    @Override
    public boolean validForPass(int i) {
        return true; //TODO
    }

    @Override
    public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
    	if(externalSystem != null && input != null){
    		IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), src);
    		if(monitor == null)
    			return null;
    		IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    		if(fluidInventory == null)
    			return null;
    		return fluidInventory.injectItems(input, mode, src);
    	}
        if (tank == null || input == null || !canAccept(input))
            return input;
        FluidStack toFill = input.getFluidStack();
        int filled = 0;
        int filled2 = 0;
        do {
			filled2 = tank.fill(node.getSide().getOpposite(), new FluidStack(toFill.fluidID, toFill.amount - filled), mode == Actionable.MODULATE);
			filled = filled + filled2;
		} while (filled2 != 0 && filled != toFill.amount);
        if (filled == toFill.amount)
            return null;
        return FluidUtil.createAEFluidStack(toFill.fluidID, toFill.amount - filled);
    }

    @Override
    public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
    	if(!node.isActive())
    		return null;
    	if(externalSystem != null && request != null && (AccessRestriction.WRITE != access || AccessRestriction.NO_ACCESS != access)){
    		IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), src);
    		if(monitor == null)
    			return null;
    		IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    		if(fluidInventory == null)
    			return null;
    		return fluidInventory.extractItems(request, mode, src);
    			
    	}
    	if (tank == null || request == null || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS)
            return null;
    	FluidStack toDrain = request.getFluidStack();
        int drained = 0;
        int drained2 = 0;
        do {
        	FluidStack drain = tank.drain(node.getSide().getOpposite(), new FluidStack(toDrain.fluidID, toDrain.amount - drained), mode == Actionable.MODULATE);
        	if(drain == null)
        		drained2 = 0;
        	else
        		drained2 = drain.amount;
        	drained = drained + drained2;
		} while (toDrain.amount != drained && drained2 != 0);
        if(drained == 0)
        	return null;
        if (drained == toDrain.amount)
        	return request;
        return FluidUtil.createAEFluidStack(toDrain.fluidID, drained);
    }

    @Override
    public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
    	if(!node.isActive())
    		return out;
    	if(externalSystem != null){
    		IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), new MachineSource(node));
    		if(monitor == null)
    			return out;
    		IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    		if(fluidInventory == null)
    			return out;
    		IItemList<IAEFluidStack> list = externalSystem.getMonitorable(node.getSide().getOpposite(), new MachineSource(node)).getFluidInventory().getStorageList();
    		for(IAEFluidStack stack : list){
    			out.add(stack);
    		}
    	}else if (tank != null) {
            FluidTankInfo[] infoArray = tank.getTankInfo(node.getSide().getOpposite());
            if (infoArray != null && infoArray.length > 0)
                out.add(AEApi.instance().storage().createFluidStack(infoArray[0].fluid));
        }
        return out;
    }

    @Override
    public StorageChannel getChannel() {
        return StorageChannel.FLUIDS;
    }

    public void onNeighborChange() {
    	if(externalSystem != null){
    		IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), new MachineSource(node));
    		if(monitor != null){
    			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    			if(fluidInventory != null){
    				fluidInventory.removeListener(this);
    			}
    		}
    	}
        tank = null;
        ForgeDirection orientation = node.getSide();
        TileEntity hostTile = node.getHostTile();
        if (hostTile == null)
            return;
        if (hostTile.getWorldObj() == null)
            return;
        TileEntity tileEntity = hostTile.getWorldObj().getTileEntity(hostTile.xCoord + orientation.offsetX, hostTile.yCoord + orientation.offsetY, hostTile.zCoord + orientation.offsetZ);
        tank = null;
        externalSystem =  null;
        if(tileEntity instanceof ITileStorageMonitorable){
        	externalSystem = (ITileStorageMonitorable) tileEntity;
        	IStorageMonitorable monitor = externalSystem.getMonitorable(node.getSide().getOpposite(), new MachineSource(node));
    		if(monitor == null)
    			return;
    		IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
    		if(fluidInventory == null)
    			return;
    		fluidInventory.addListener(this, null);
    		return;
        }
        if (tileEntity instanceof IFluidHandler)
            tank = (IFluidHandler) tileEntity;
    }

    public void setInverted(boolean _inverted) {
        inverted = _inverted;
    }

    public void setPrioritizedFluids(Fluid[] _fluids) {
    	prioritizedFluids.clear();
    	for(Fluid fluid : _fluids){
    		if(fluid != null)
    			prioritizedFluids.add(fluid);
    	}
    }

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor, Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
		IGridNode gridNode = node.getGridNode();
        if (gridNode != null) {
            IGrid grid = gridNode.getGrid();
            if (grid != null) {
                grid.postEvent(new MENetworkCellArrayUpdate());
                gridNode.getGrid().postEvent(new MENetworkStorageEvent(node.getGridBlock().getFluidMonitor(), StorageChannel.FLUIDS));
            }
            node.getHost().markForUpdate();
        }
	}

	@Override
	public void onListUpdate() {
		
	}
}
