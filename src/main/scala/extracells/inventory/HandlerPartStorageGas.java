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
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.api.ECApi;
import extracells.part.PartFluidStorage;
import extracells.util.FluidUtil;
import mekanism.api.gas.IGasHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

public class HandlerPartStorageGas extends HandlerPartStorageFluid {

	private IGasHandler tankGas;

	public HandlerPartStorageGas(PartFluidStorage _node) {
		super(_node);
	}

	@Override
	public boolean canAccept(IAEFluidStack input) {
		if (!this.node.isActive())
			return false;
		if(!ECApi.instance().isGasStack(input))
			return false;
		if (this.tank == null && this.externalSystem == null || !(this.access == AccessRestriction.WRITE || this.access == AccessRestriction.READ_WRITE))
			return false;
		/*if (this.externalSystem != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), new MachineSource(this.node));
			if (monitor == null)
				return false;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return false;
			return fluidInventory.canAccept(input);
		}*/
		FluidTankInfo[] infoArray = null;
		if (infoArray != null && infoArray.length > 0) {
			FluidTankInfo info = infoArray[0];
			if (info.fluid == null || info.fluid.amount == 0 || info.fluid.getFluidID() == input.getFluidStack().getFluidID())
				if (this.inverted)
					return !this.prioritizedFluids.isEmpty() || !isPrioritized(input);
				else
					return this.prioritizedFluids.isEmpty() || isPrioritized(input);
		}
		return false;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode,
			BaseActionSource src) {
		if (!this.node.isActive() || !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return null;
		if (this.externalSystem != null && request != null) {
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(this.node.getSide().getOpposite(), src);
			if (monitor == null)
				return null;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return null;
			return fluidInventory.extractItems(request, mode, src);

		}
		if (this.tank == null || request == null || this.access == AccessRestriction.WRITE || this.access == AccessRestriction.NO_ACCESS)
			return null;
		FluidStack toDrain = request.getFluidStack();
		int drained = 0;
		int drained2 = 0;
		do {
			FluidStack drain = this.tank.drain(this.node.getSide().getOpposite(), new FluidStack(toDrain.getFluidID(), toDrain.amount - drained), mode == Actionable.MODULATE);
			if (drain == null)
				drained2 = 0;
			else
				drained2 = drain.amount;
			drained = drained + drained2;
		} while (toDrain.amount != drained && drained2 != 0);
		if (drained == 0)
			return null;
		if (drained == toDrain.amount)
			return request;
		return FluidUtil.createAEFluidStack(toDrain.getFluidID(), drained);
	}

	@Override
	public AccessRestriction getAccess() {
		return this.access;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
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
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src) {
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
		}
		if (this.tank == null || input == null || !canAccept(input))
			return input;
		FluidStack toFill = input.getFluidStack();
		int filled = 0;
		int filled2 = 0;
		do {
			filled2 = this.tank.fill(this.node.getSide().getOpposite(), new FluidStack(toFill.getFluid(), toFill.amount - filled), mode == Actionable.MODULATE);
			filled = filled + filled2;
		} while (filled2 != 0 && filled != toFill.amount);
		if (filled == toFill.amount)
			return null;
		return FluidUtil.createAEFluidStack(toFill.getFluidID(), toFill.amount - filled);
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input) {
		if (input == null)
			return false;
		for (Fluid fluid : this.prioritizedFluids)
			if (fluid == input.getFluid())
				return true;
		return false;
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
			IStorageMonitorable monitor = this.externalSystem.getMonitorable(
					this.node.getSide().getOpposite(), new MachineSource(
							this.node));
			if (monitor != null) {
				IMEMonitor<IAEFluidStack> fluidInventory = monitor
						.getFluidInventory();
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
		this.tank = null;
		this.externalSystem = null;
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
			return;
		}
		if (tileEntity instanceof IFluidHandler)
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
				gridNode.getGrid().postEvent(
						new MENetworkStorageEvent(this.node.getGridBlock()
								.getFluidMonitor(), StorageChannel.FLUIDS));
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
