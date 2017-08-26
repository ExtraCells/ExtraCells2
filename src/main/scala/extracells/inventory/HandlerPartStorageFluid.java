package extracells.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

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
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IMEMonitorHandlerReceiver;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.integration.Capabilities;
import extracells.part.fluid.PartFluidStorage;
import extracells.util.FluidUtil;

public class HandlerPartStorageFluid implements IMEInventoryHandler<IAEFluidStack>, IMEMonitorHandlerReceiver<IAEFluidStack> {

	protected PartFluidStorage node;
	protected IFluidHandler tank;
	protected AccessRestriction access = AccessRestriction.READ_WRITE;
	protected List<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	protected boolean inverted;
	private IStorageMonitorableAccessor externalHandler = null;
	protected TileEntity tile = null;
	public IStorageMonitorableAccessor externalSystem;

	public HandlerPartStorageFluid(PartFluidStorage _node) {
		this.node = _node;
	}

	@Override
	public boolean canAccept(IAEFluidStack input) {
		if (!this.node.isActive())
			return false;
		if (this.tank == null && this.externalSystem == null && this.externalHandler == null || !(this.access == AccessRestriction.WRITE || this.access == AccessRestriction.READ_WRITE) || input == null)
			return false;
		if (this.externalSystem != null) {
			IStorageMonitorable monitor = this.externalSystem.getInventory(new MachineSource(this.node));
			if (monitor == null)
				return false;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor
					.getFluidInventory();
			return fluidInventory != null && fluidInventory.canAccept(input);
		}else if(externalHandler != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(new MachineSource(this.node)).getFluidInventory();
			return inventory != null;
		}
		IFluidTankProperties[] infoArray = this.tank.getTankProperties();
		if (infoArray != null && infoArray.length > 0) {
			IFluidTankProperties info = infoArray[0];
			if (info.getContents() == null || info.getContents().amount == 0 || info.getContents().getFluid().getName().equals(input.getFluidStack().getFluid().getName()))
				if (this.inverted)
					return !this.prioritizedFluids.isEmpty() || !isPrioritized(input);
				else
					return this.prioritizedFluids.isEmpty() || isPrioritized(input);
		}
		return false;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src) {
		if (!this.node.isActive()
				|| !(this.access == AccessRestriction.READ || this.access == AccessRestriction.READ_WRITE))
			return null;
		if (this.externalSystem != null && request != null) {
			IStorageMonitorable monitor = this.externalSystem.getInventory(src);
			if (monitor == null)
				return null;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor
					.getFluidInventory();
			if (fluidInventory == null)
				return null;
			return fluidInventory.extractItems(request, mode, src);

		}else if(externalHandler != null && request != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(new MachineSource(this.node)).getFluidInventory();
			if(inventory == null)
				return null;
			return inventory.extractItems(request, mode, new MachineSource(this.node));
		}
		if (this.tank == null || request == null || this.access == AccessRestriction.WRITE || this.access == AccessRestriction.NO_ACCESS)
			return null;
		FluidStack toDrain = request.getFluidStack();
		int drained = 0;
		int drained2 = 0;
		do {
			FluidStack drain = this.tank.drain(new FluidStack(toDrain.getFluid(), toDrain.amount - drained), mode == Actionable.MODULATE);
			if (drain == null)
				drained2 = 0;
			else
				drained2 = drain.amount;
			drained = drained + drained2;
		} while (toDrain.amount != drained && drained2 != 0);
		if (drained == 0)
			return null;
		IItemList<IAEFluidStack> fluids = getAvailableItems(AEApi.instance().storage().createFluidList());
		for(IAEFluidStack fluid : fluids){
			if(fluid.getFluid() == request.getFluid()){
				drained = (int) Math.min(drained, fluid.getStackSize());
			}
		}

		if (drained == toDrain.amount)
			return request;
		return FluidUtil.createAEFluidStack(toDrain.getFluid().getName(), drained);
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
			IStorageMonitorable monitor = this.externalSystem.getInventory(new MachineSource(this.node));
			if (monitor == null)
				return out;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return out;
			IItemList<IAEFluidStack> list = this.externalSystem.getInventory(new MachineSource(this.node)).getFluidInventory().getStorageList();
			for (IAEFluidStack stack : list) {
				out.add(stack);
			}
		}else if(externalHandler != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(new MachineSource(this.node)).getFluidInventory();
			if(inventory == null)
				return out;
			IItemList<IAEFluidStack> list = inventory.getAvailableItems(AEApi.instance().storage().createFluidList());
			for(IAEFluidStack stack : list){
				out.add(stack);
			}
		} else if (this.tank != null) {
			IFluidTankProperties[] infoArray = this.tank.getTankProperties();
			if (infoArray != null && infoArray.length > 0){
				for(IFluidTankProperties info : infoArray){
					if(info.getContents() != null)
						out.add(AEApi.instance().storage().createFluidStack(info.getContents()));
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
			IStorageMonitorable monitor = this.externalSystem.getInventory(src);
			if (monitor == null)
				return input;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return input;
			return fluidInventory.injectItems(input, mode, src);
		}else if(externalHandler != null && input != null){
			IMEInventory<IAEFluidStack> inventory = externalHandler.getInventory(new MachineSource(this.node)).getFluidInventory();
			if(inventory == null)
				return input;
			return inventory.injectItems(input, mode, new MachineSource(this.node));
		}
		if (this.tank == null || input == null || !canAccept(input))
			return input;
		FluidStack toFill = input.getFluidStack();
		int filled = 0;
		int filled2 = 0;
		do {
			filled2 = this.tank.fill(new FluidStack(toFill.getFluid(), toFill.amount - filled), mode == Actionable.MODULATE);
			filled = filled + filled2;
		} while (filled2 != 0 && filled != toFill.amount);
		IFluidTankProperties[] infos = this.tank.getTankProperties();
		int maxFill = 0;
		for(IFluidTankProperties info : infos){
			if(info.getContents() == null)
				maxFill += info.getCapacity();
			else if(info.getContents().getFluid() == toFill.getFluid())
				maxFill += info.getCapacity() - info.getContents().amount;
		}
		filled = Math.min(filled, maxFill);
		if (filled == toFill.amount)
			return null;
		return FluidUtil.createAEFluidStack(toFill.getFluid().getName(), toFill.amount - filled);
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
			IStorageMonitorable monitor = this.externalSystem.getInventory(new MachineSource(this.node));
			if (monitor != null) {
				IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
				if (fluidInventory != null) {
					fluidInventory.removeListener(this);
				}
			}
		}
		this.tank = null;
		TileEntity hostTile = this.node.getHostTile();
		if (hostTile == null) {
			return;
		}
		EnumFacing orientation = this.node.getFacing();
		if (hostTile.getWorld() == null) {
			return;
		}
		TileEntity tileEntity = hostTile.getWorld().getTileEntity(
				hostTile.getPos().offset(orientation));
		this.tile = tileEntity;
		this.tank = null;
		this.externalSystem = null;
		if(tileEntity == null){
			this.externalHandler = null;
			return;
		}
		if (tileEntity.hasCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, node.getFacing().getOpposite())) {
			IStorageMonitorable monitor = null;
			externalSystem = tileEntity.getCapability(Capabilities.STORAGE_MONITORABLE_ACCESSOR, node.getFacing().getOpposite());
			monitor = externalSystem.getInventory(new MachineSource(this.node));
			if (monitor == null)
				return;
			IMEMonitor<IAEFluidStack> fluidInventory = monitor.getFluidInventory();
			if (fluidInventory == null)
				return;
			fluidInventory.addListener(this, null);

		}else if (externalHandler == null && tileEntity.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, node.getFacing().getOpposite())){
			tank = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, node.getFacing().getOpposite());
		}
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
