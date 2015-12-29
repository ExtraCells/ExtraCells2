package extracells.inventory;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.events.MENetworkCellArrayUpdate;
import appeng.api.networking.events.MENetworkStorageEvent;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.MachineSource;
import appeng.api.networking.storage.IBaseMonitor;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.api.ECApi;
import extracells.api.IExternalGasStorageHandler;
import extracells.part.PartFluidStorage;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

public class HandlerPartStorageGas extends HandlerPartStorageFluid {

	private IExternalGasStorageHandler externalHandler = null;

	public HandlerPartStorageGas(PartFluidStorage _node) {
		super(_node);
	}

	@Override
	public boolean canAccept(IAEFluidStack input) {
		if (!this.node.isActive())
			return false;
		if (this.tank == null && this.externalSystem == null && this.externalHandler == null || !(this.access == AccessRestriction.WRITE || this.access == AccessRestriction.READ_WRITE) || input == null)
			return false;
		if(externalHandler != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), new MachineSource(this.node));
			if (inventory == null)
				return false;
		}else
			return false;
		if (this.inverted)
			return !this.prioritizedFluids.isEmpty() || !isPrioritized(input);
		else
			return this.prioritizedFluids.isEmpty() || isPrioritized(input);
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
		if (!this.node.isActive() || !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return null;
		if(externalHandler != null && request != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), new MachineSource(this.node));
			if(inventory == null)
				return null;
			return inventory.extractItems(request, mode, new MachineSource(this.node));
		}
		return null;
	}

	@Override
	public AccessRestriction getAccess() {
		return this.access;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out) {
		if (!this.node.isActive() || !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return out;
		if(externalHandler != null) {
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), new MachineSource(this.node));
			if (inventory == null)
				return out;
			IItemList<IAEFluidStack> list = inventory.getAvailableItems(AEApi.instance().storage().createFluidList());
			for (IAEFluidStack stack : list) {
				out.add(stack);
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
			return input;
		if(externalHandler != null && input != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(this.tile, this.node.getSide().getOpposite(), new MachineSource(this.node));
			if(inventory == null)
				return null;
			return inventory.injectItems(input, mode, new MachineSource(this.node));
		}
		return input;
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
		this.externalHandler = ECApi.instance().getHandler(tileEntity, this.node.getSide().getOpposite(), new MachineSource(this.node));
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
