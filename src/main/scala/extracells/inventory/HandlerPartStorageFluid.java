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
import appeng.api.storage.*;
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
import java.util.List;

public class HandlerPartStorageFluid implements IMEInventoryHandler<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

	protected PartFluidStorage node;
	protected IFluidHandler tank;
	protected AccessRestriction access = AccessRestriction.READ_WRITE;
	protected List<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	protected boolean inverted;
	private  IExternalStorageHandler externalHandler = null;
	protected TileEntity tile = null;
	public ITileStorageMonitorable externalSystem;

	public HandlerPartStorageFluid(PartFluidStorage _node) {
		this.node = _node;
	}

	@Override
	public boolean canAccept(IAEFluidStack input) {
		if (!this.node.isActive())
			return false;
		else if (this.tank == null && this.externalSystem == null && this.externalHandler == null || !(this.access == AccessRestriction.WRITE || this.access == AccessRestriction.READ_WRITE) || input == null)
			return false;
		else if (this.externalSystem != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(
					this.node.getSide().getOpposite(), new MachineSource(
							this.node));
			if (monitor == null)
				return false;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor
					.getFluidInventory();
			return fluidInventory != null && fluidInventory.canAccept(input);
		} else if (externalHandler != null) {
			IMEInventory<IAEFluidStack> inventory = this.externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), StorageChannel.FLUIDS, new MachineSource(this.node));
			return inventory != null;
		}
		FluidTankInfo[] infoArray = this.tank.getTankInfo(this.node.getSide().getOpposite());
		if (infoArray != null && infoArray.length > 0) {
			for (FluidTankInfo tank : infoArray) {
			    if (tank.fluid == null) return isPrioritized(input);
			    else if (tank.fluid.getFluidID() == input.getFluidStack().getFluidID()) {
			        if (!isPrioritized(input)) return false;
                    if (tank.fluid.amount < tank.capacity) return true;
                }
            }
		}
		return false;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
		if (!this.node.isActive()
				|| !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return null;
		if (this.externalSystem != null && request != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(
					this.node.getSide().getOpposite(), src);
			if (monitor == null)
				return null;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor
					.getFluidInventory();
			if (fluidInventory == null)
				return null;
			return fluidInventory.extractItems(request, mode, src);

		}else if(externalHandler != null && request != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), StorageChannel.FLUIDS, new MachineSource(this.node));
			if(inventory == null)
				return null;
			return inventory.extractItems(request, mode, new MachineSource(this.node));
		}
		if (this.tank == null || request == null || this.access == AccessRestriction.WRITE || this.access == AccessRestriction.NO_ACCESS)
			return null;
		FluidStack toDrain = request.getFluidStack();
		if (!this.tank.canDrain(this.node.getSide().getOpposite(), toDrain.getFluid()))
		    return null;

        FluidStack drain = this.tank.drain(
        		this.node.getSide().getOpposite(), new FluidStack(toDrain.getFluid(), toDrain.amount), mode == Actionable.MODULATE);

        if (drain == null || !drain.getFluid().equals(request.getFluid())) {
        	return null;
		} else if (drain.amount == 0) {
			return null;
		} else if (drain.amount == toDrain.amount) {
			return request;
		} else {
			return FluidUtil.createAEFluidStack(toDrain.getFluidID(), drain.amount);
		}
	}

	@Override
	public AccessRestriction getAccess() {
		return this.access;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(
			IItemList<IAEFluidStack> out) {
		if (!this.node.isActive() || !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return out;
		if (this.externalSystem != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), new MachineSource(this.node));
			if (monitor == null)
				return out;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return out;
			IItemList<IAEFluidStack> list = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), new MachineSource(this.node)).getFluidInventory().getStorageList();
			for (IAEFluidStack stack : list) {
				out.add(stack);
			}
		}else if(externalHandler != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), StorageChannel.FLUIDS, new MachineSource(this.node));
			if(inventory == null)
				return out;
			IItemList<IAEFluidStack> list = inventory.getAvailableItems(AEApi.instance().storage().createFluidList());
			for(IAEFluidStack stack : list){
				out.add(stack);
			}
		} else if (this.tank != null) {
			FluidTankInfo[] infoArray = this.tank.getTankInfo(this.node.getSide().getOpposite());
			if (infoArray != null && infoArray.length > 0){
				for(FluidTankInfo info : infoArray){
					if(info.fluid != null)
						out.add(AEApi.instance().storage().createFluidStack(info.fluid));
				}
			}
		}
		return out;
	}

	@Override
	public StorageChannel getChannel() {
		return StorageChannel.FLUIDS;
	}

	@Override
	public int getPriority() {
		return this.node.getPriority();
	}

	@Override
	public int getSlot() {
		return 0;
	}

	@Override
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode,
			BaseActionSource src) {
		if (!(this.access == AccessRestriction.WRITE || this.access == AccessRestriction.READ_WRITE))
			return null;
		if (this.externalSystem != null && input != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), src);
			if (monitor == null)
				return input;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return input;
			return fluidInventory.injectItems(input, mode, src);
		}else if(externalHandler != null && input != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), StorageChannel.FLUIDS, new MachineSource(this.node));
			if(inventory == null)
				return input;
			return inventory.injectItems(input, mode, new MachineSource(this.node));
		}
		if (this.tank == null || input == null || !canAccept(input))
			return input;
		FluidStack toFill = input.getFluidStack();
		int filled = this.tank.fill(this.node.getSide().getOpposite(), new FluidStack(toFill.getFluid(), toFill.amount), mode == Actionable.MODULATE);
		if (filled == toFill.amount)
			return null;
		return FluidUtil.createAEFluidStack(toFill.getFluidID(), toFill.amount - filled);
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input) {
		if (input == null)
			return false;
		else if (this.prioritizedFluids.isEmpty()) return true;

		for (Fluid fluid : this.prioritizedFluids)
			if (fluid == input.getFluid())
				return !this.inverted;
		return this.inverted;
	}

	@Override
	public boolean isValid(Object verificationToken) {
		return true;
	}

	@Override
	public void onListUpdate() {

	}

	public void onNeighborChange() {
		if (this.externalSystem != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), new MachineSource(this.node));
			if (monitor != null) {
				IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
				if (fluidInventory != null) {
					fluidInventory.removeListener(this);
				}
			}
		}
		this.tank = null;
		ForgeDirection orientation = this.node.getSide();
		TileEntity hostTile = this.node.getHostTile();
		if (hostTile == null)
			return;
		if (hostTile.getWorldObj() == null)
			return;
		TileEntity tileEntity = hostTile.getWorldObj().getTileEntity(
				hostTile.xCoord + orientation.offsetX,
				hostTile.yCoord + orientation.offsetY,
				hostTile.zCoord + orientation.offsetZ);
		this.tile = tileEntity;
		this.tank = null;
		this.externalSystem = null;
		if(tileEntity == null){
			this.externalHandler = null;
			return;
		}
		this.externalHandler = AEApi.instance().registries().externalStorage().getHandler(tileEntity, this.node.getSide().getOpposite(), StorageChannel.FLUIDS, new MachineSource(this.node));
		if (tileEntity instanceof ITileStorageMonitorable) {
			this.externalSystem = (ITileStorageMonitorable) tileEntity;
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(
					this.node.getSide().getOpposite(), new MachineSource(
							this.node));
			if (monitor == null)
				return;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor
					.getFluidInventory();
			if (fluidInventory == null)
				return;
			fluidInventory.addListener(this, null);

		}else if (externalHandler == null && tileEntity instanceof IFluidHandler)
			this.tank = (IFluidHandler) tileEntity;
	}

	@Override
	public void postChange(IBaseMonitor<IAEFluidStack> monitor,
			Iterable<IAEFluidStack> change, BaseActionSource actionSource) {
		IGridNode gridNode = this.node.getGridNode();
		if (gridNode != null) {
			IGrid grid = gridNode.getGrid();
			if (grid != null) {
				grid.postEvent(new MENetworkCellArrayUpdate());
				gridNode.getGrid().postEvent(new MENetworkStorageEvent(this.node.getGridBlock().getFluidMonitor(), StorageChannel.FLUIDS));
			}
			this.node.getHost().markForUpdate();
		}
	}

	public void setAccessRestriction(AccessRestriction access) {
		this.access = access;
	}

	public void setInverted(boolean _inverted) {
		this.inverted = _inverted;
	}

	public void setPrioritizedFluids(Fluid[] _fluids) {
		this.prioritizedFluids.clear();
		for (Fluid fluid : _fluids) {
			if (fluid != null)
				this.prioritizedFluids.add(fluid);
		}
	}

	@Override
	public boolean validForPass(int i) {
		return true; // TODO
	}
}
