package extracells.inventory.cell;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import extracells.util.Log;
import extracells.util.StorageChannels;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.api.ECApi;
import extracells.api.IFluidStorageCell;
import extracells.api.IHandlerFluidStorage;

//TODO: rewrite
public class HandlerItemStorageFluid implements ICellInventoryHandler<IAEFluidStack>, IHandlerFluidStorage, ICellInventory<IAEFluidStack> {

	private ItemStack storageStack;
	private NBTTagCompound stackTag;
	protected ArrayList<FluidStack> fluidStacks = new ArrayList<FluidStack>();
	private ArrayList<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	private int totalTypes;
	private int totalBytes;

	private int itemsPerByte;

	private boolean _dirty = true;
	private long storedCount;

	private ISaveProvider saveProvider;

	public HandlerItemStorageFluid(ItemStack _storageStack,
		ISaveProvider _saveProvider) {
		if (!_storageStack.hasTagCompound()) {
			_storageStack.setTagCompound(new NBTTagCompound());
		}
		this.storageStack = _storageStack;
		this.stackTag = _storageStack.getTagCompound();
		this.totalTypes = ((IFluidStorageCell) _storageStack.getItem()).getMaxTypes(_storageStack);
		this.totalBytes = ((IFluidStorageCell) _storageStack.getItem()).getMaxBytes(_storageStack);
		this.itemsPerByte = this.getChannel().getUnitsPerByte();

		for (int i = 0; i < this.totalTypes; i++) {
			this.fluidStacks.add(FluidStack.loadFluidStackFromNBT(this.stackTag.getCompoundTag("Fluid#" + i)));
		}

		this.saveProvider = _saveProvider;
	}

	public HandlerItemStorageFluid(ItemStack _storageStack, ISaveProvider _saveProvider, ArrayList<Fluid> _filter) {
		this(_storageStack, _saveProvider);
		if (_filter != null) {
			this.prioritizedFluids = _filter;
		}
	}

	private boolean allowedByFormat(Fluid fluid) {
		return !isFormatted() || this.prioritizedFluids.contains(fluid);
	}

	@Override
	public boolean canAccept(IAEFluidStack input) {
		if (input == null) {
			return false;
		}
		if (!ECApi.instance().canStoreFluid(input.getFluid())) {
			return false;
		}
		for (FluidStack fluidStack : this.fluidStacks) {
			if (fluidStack == null || fluidStack.getFluid() == input.getFluid()) {
				return allowedByFormat(input.getFluid());
			}
		}
		return false;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode,
		IActionSource src) {
		if (request == null || !allowedByFormat(request.getFluid())) {
			return null;
		}

		IAEFluidStack removedStack;
		List<FluidStack> currentFluids = Lists.newArrayList(this.fluidStacks);
		for (int i = 0; i < this.fluidStacks.size(); i++) {
			FluidStack currentStack = this.fluidStacks.get(i);
			if (currentStack != null && currentStack.getFluid().getName().equals(request.getFluid().getName())) {
				long endAmount = currentStack.amount - request.getStackSize();
				if (endAmount >= 0) {
					removedStack = request.copy();
					FluidStack toWrite = new FluidStack(currentStack.getFluid(), (int) endAmount);
					currentFluids.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, toWrite);
					}
				} else {
					removedStack = StorageChannels.FLUID().createStack(currentStack.copy());
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, null);
					}
				}
				if (removedStack != null && removedStack.getStackSize() > 0) {
					this._dirty = true;
					requestSave();
				}
				return removedStack;
			}
		}

		return null;
	}

	@Override
	public AccessRestriction getAccess() {
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(
		IItemList<IAEFluidStack> out) {
		for (FluidStack fluidStack : this.fluidStacks) {
			if (fluidStack != null) {
				out.add(StorageChannels.FLUID().createStack(fluidStack));
			}
		}
		return out;
	}

	@Override
	public IStorageChannel getChannel() {
		return StorageChannels.FLUID();
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public int getSlot() {
		return 0;
	}

	@Override
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, IActionSource src) {
		if (input == null) {
			return input;
		}
		if (input.getStackSize() == 0) {
			return input;
		}
		if (!allowedByFormat(input.getFluid())) {
			return input;
		}
		IAEFluidStack notAdded = input.copy();
		List<FluidStack> currentFluids = Lists.newArrayList(this.fluidStacks);
		for (int i = 0; i < currentFluids.size(); i++) {
			FluidStack currentStack = currentFluids.get(i);
			if (notAdded != null && currentStack != null
				&& input.getFluid() == currentStack.getFluid()) {
				if (notAdded.getStackSize() <= remainingItems()) {
					FluidStack toWrite = new FluidStack(currentStack.getFluid(),
						currentStack.amount + (int) notAdded.getStackSize());
					currentFluids.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, toWrite);
					}
					notAdded = null;
				} else {
					FluidStack toWrite = new FluidStack(currentStack.getFluid(), currentStack.amount + remainingItems());
					currentFluids.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, toWrite);
					}
					notAdded.setStackSize(notAdded.getStackSize() - remainingItems());
				}
			}
		}
		for (int i = 0; i < currentFluids.size(); i++) {
			FluidStack currentStack = currentFluids.get(i);
			if (notAdded != null && currentStack == null) {
				if (input.getStackSize() <= getRemainingItemCount()) {
					FluidStack toWrite = notAdded.getFluidStack();
					currentFluids.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, toWrite);
					}
					notAdded = null;
				} else {
					FluidStack toWrite = new FluidStack(notAdded.getFluid(), remainingItems());
					currentFluids.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeFluidToSlot(i, toWrite);
					}
					notAdded.setStackSize(notAdded.getStackSize() - remainingItems());
				}
			}
		}
		if (notAdded == null || !notAdded.equals(input)) {
			this._dirty = true;
			requestSave();
		}
		return notAdded;
	}

	@Override
	public boolean isFormatted() {
		// Common case
		if (this.prioritizedFluids.isEmpty()) {
			return false;
		}

		for (Fluid currentFluid : this.prioritizedFluids) {
			if (currentFluid != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input) {
		return input != null
			&& this.prioritizedFluids.contains(input.getFluid());
	}

	private void requestSave() {
		if (this.saveProvider != null) {
			this.saveProvider.saveChanges(this);
		}
	}

	@Override
	public int totalBytes() {
		return this.totalBytes;
	}

	@Override
	public int totalTypes() {
		return this.totalTypes;
	}

	@Override
	public int usedBytes() {
		return this.getBytesPerType() * this.usedTypes()
		    + (int) ((this.getStoredItemCount() + this.getUnusedItemCount()) / this.itemsPerByte);
	}

	@Override
	public int usedTypes() {
		int i = 0;
		for (FluidStack stack : this.fluidStacks) {
			if (stack != null) {
				i++;
			}
		}
		return i;
	}

	@Override
	public long storedCount() {
		return this.getStoredItemCount();
	}

	public int remainingItems() {
		return this.getRemainingItemCount() > Integer.MAX_VALUE
		     ? (int) Integer.MAX_VALUE
				 : (int) this.getRemainingItemCount();
	}

	@Override
	public boolean validForPass(int i) {
		return true; // TODO
	}

	protected void writeFluidToSlot(int i, FluidStack fluidStack) {
		NBTTagCompound fluidTag = new NBTTagCompound();
		if (fluidStack != null && fluidStack.amount > 0) {
			fluidStack.writeToNBT(fluidTag);
			this.stackTag.setTag("Fluid#" + i, fluidTag);
		} else {
			this.stackTag.removeTag("Fluid#" + i);
		}
		this.fluidStacks.set(i, fluidStack);
	}

	@Override
	public ICellInventory<IAEFluidStack> getCellInv() {
		return this;
	}

	@Override
	public boolean isPreformatted() {
		return this.isFormatted();
	}

	@Override
	public boolean isFuzzy() {
		return this.getFuzzyMode() != FuzzyMode.IGNORE_ALL;
	}

	@Override
	public IncludeExclude getIncludeExcludeMode() {
		return IncludeExclude.WHITELIST;
	}

	@Override
	public ItemStack getItemStack() {
		return this.storageStack;
	}

	protected ICellWorkbenchItem getCellItem() {
		return (ICellWorkbenchItem) this.getItemStack().getItem();
	}

	@Override
	public double getIdleDrain() {
		return 1;
	}

	@Override
	public FuzzyMode getFuzzyMode() {
		return this.getCellItem().getFuzzyMode(this.getItemStack());
	}

	@Override
	public IItemHandler getConfigInventory() {
		return this.getCellItem().getConfigInventory(this.getItemStack());
	}

	@Override
	public IItemHandler getUpgradesInventory() {
		return this.getCellItem().getUpgradesInventory(this.getItemStack());
	}

	@Override
	public int getBytesPerType() {
		return totalBytes / 128;
	}

	@Override
	public boolean canHoldNewItem() {
		long bytesFree = this.getFreeBytes();
		return ( bytesFree > this.getBytesPerType() || ( bytesFree == this.getBytesPerType() && this.getUnusedItemCount() > 0 ) ) && this
				.getRemainingItemTypes() > 0;
	}

	@Override
	public long getTotalBytes() {
		return this.totalBytes();
	}

	@Override
	public long getFreeBytes() {
		return this.totalBytes() - this.usedBytes();
	}

	@Override
	public long getUsedBytes() {
		return this.usedBytes();
	}

	@Override
	public long getTotalItemTypes() {
		return this.totalTypes();
	}

	private void refreshStoredItemCache() {
		this.storedCount = 0;
		for (FluidStack stack : this.fluidStacks) {
			if (stack != null) {
				this.storedCount += stack.amount;
			}
		}
		this._dirty = false;
	}

	@Override
	public long getStoredItemCount() {
		if (this._dirty) {
			this.refreshStoredItemCache();
		}
		return this.storedCount;
	}

	@Override
	public long getStoredItemTypes() {
		return this.usedTypes();
	}

	@Override
	public long getRemainingItemTypes() {
		return this.totalTypes() - this.usedTypes();
	}

	@Override
	public long getRemainingItemCount() {
		return this.getFreeBytes() * this.itemsPerByte + this.getUnusedItemCount();
	}

	@Override
	public int getUnusedItemCount() {
		final int modResult = (int) (this.getStoredItemCount() % this.itemsPerByte);
		return modResult == 0 ? 0 : this.getChannel().getUnitsPerByte() - modResult;
	}

	@Override
	public int getStatusForCell() {
		if( this.canHoldNewItem() )
		{
			return 1;
		}
		if( this.getRemainingItemCount() > 0 )
		{
			return 2;
		}
		return 3;
	}

	@Override
	public void persist() {
        if (this.saveProvider != null) {
            this.saveProvider.saveChanges(this);
        }
	}
}
