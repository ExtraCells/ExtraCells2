package extracells.inventory.cell;

import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.*;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import extracells.api.IHandlerGasStorage;
import extracells.api.gas.IAEGasStack;
import extracells.util.GasUtil;
import extracells.util.Log;
import extracells.util.StorageChannels;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.storage.data.IItemList;
import extracells.api.IGasStorageCell;

//TODO: rewrite
public class HandlerItemStorageGas implements ICellInventoryHandler<IAEGasStack>, IHandlerGasStorage, ICellInventory<IAEGasStack> {

	private ItemStack storageStack;
	private NBTTagCompound stackTag;
	protected ArrayList<GasStack> gasStacks = new ArrayList<GasStack>();
	private ArrayList<Gas> prioritizedGases = new ArrayList<Gas>();
	private int totalTypes;
	private int totalBytes;
	private ISaveProvider saveProvider;

	public HandlerItemStorageGas(ItemStack _storageStack,
		ISaveProvider _saveProvider) {
		if (!_storageStack.hasTagCompound()) {
			_storageStack.setTagCompound(new NBTTagCompound());
		}
		this.storageStack = _storageStack;
		this.stackTag = _storageStack.getTagCompound();
		this.totalTypes = ((IGasStorageCell) _storageStack.getItem()).getMaxTypes(_storageStack);
		this.totalBytes = ((IGasStorageCell) _storageStack.getItem()).getMaxBytes(_storageStack) * 250;

		for (int i = 0; i < this.totalTypes; i++) {
			if (this.stackTag.hasKey("Gas#" + i))
				this.gasStacks.add(GasStack.readFromNBT(this.stackTag.getCompoundTag("Gas#" + i)));
			else
				//Load From old fluid Tags
				this.gasStacks.add(GasUtil.getGasStack(FluidStack.loadFluidStackFromNBT(this.stackTag.getCompoundTag("Fluid#" + i))));
		}

		this.saveProvider = _saveProvider;
	}

	public HandlerItemStorageGas(ItemStack _storageStack, ISaveProvider _saveProvider, ArrayList<Gas> _filter) {
		this(_storageStack, _saveProvider);
		if (_filter != null) {
			this.prioritizedGases = _filter;
		}
	}

	private boolean allowedByFormat(Gas gas) {
		return !isFormatted() || this.prioritizedGases.contains(gas);
	}

	@Override
	public boolean canAccept(IAEGasStack input) {
		if (input == null) {
			return false;
		}
		for (GasStack gasStack : this.gasStacks) {
			if (gasStack == null || gasStack.getGas() == input.getGas()) {
				return allowedByFormat((Gas) input.getGas());
			}
		}
		return false;
	}

	@Override
	public IAEGasStack extractItems(IAEGasStack request, Actionable mode,
		IActionSource src) {
		if (request == null || !allowedByFormat((Gas) request.getGas())) {
			return null;
		}

		IAEGasStack removedStack;
		List<GasStack> currentGases = Lists.newArrayList(this.gasStacks);
		for (int i = 0; i < this.gasStacks.size(); i++) {
			GasStack currentStack = this.gasStacks.get(i);
			if (currentStack != null && currentStack.getGas().getName().equals(((Gas) request.getGas()).getName())) {
				long endAmount = currentStack.amount - request.getStackSize();
				if (endAmount >= 0) {
					removedStack = request.copy();
					GasStack toWrite = new GasStack(currentStack.getGas(), (int) endAmount);
					currentGases.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, toWrite);
					}
				} else {
					removedStack = StorageChannels.GAS().createStack(currentStack.copy());
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, null);
					}
				}
				if (removedStack != null && removedStack.getStackSize() > 0) {
					requestSave();
				}
				return removedStack;
			}
		}

		return null;
	}

	public int freeBytes() {
		int i = 0;
		for (GasStack stack : this.gasStacks) {
			if (stack != null) {
				i += stack.amount;
			}
		}
		return this.totalBytes - i;
	}

	@Override
	public AccessRestriction getAccess() {
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public IItemList<IAEGasStack> getAvailableItems(
		IItemList<IAEGasStack> out) {
		for (GasStack gasStack : this.gasStacks) {
			if (gasStack != null) {
				out.add(StorageChannels.GAS().createStack(gasStack));
			}
		}
		return out;
	}

	@Override
	public IStorageChannel getChannel() {
		return StorageChannels.GAS();
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
	public IAEGasStack injectItems(IAEGasStack input, Actionable mode,
		IActionSource src) {
		if (input == null || !allowedByFormat((Gas) input.getGas())) {
			return input;
		}
		IAEGasStack notAdded = input.copy();
		List<GasStack> currentGases = Lists.newArrayList(this.gasStacks);
		for (int i = 0; i < currentGases.size(); i++) {
			GasStack currentStack = currentGases.get(i);
			if (notAdded != null && currentStack != null
				&& input.getGas() == currentStack.getGas()) {
				if (notAdded.getStackSize() <= freeBytes()) {
					GasStack toWrite = new GasStack(currentStack.getGas(),
						currentStack.amount + (int) notAdded.getStackSize());
					currentGases.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, toWrite);
					}
					notAdded = null;
				} else {
					GasStack toWrite = new GasStack(currentStack.getGas(), currentStack.amount + freeBytes());
					currentGases.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, toWrite);
					}
					notAdded.setStackSize(notAdded.getStackSize() - freeBytes());
				}
			}
		}
		for (int i = 0; i < currentGases.size(); i++) {
			GasStack currentStack = currentGases.get(i);
			if (notAdded != null && currentStack == null) {
				if (input.getStackSize() <= freeBytes()) {
					GasStack toWrite = (GasStack) notAdded.getGasStack();
					currentGases.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, toWrite);
					}
					notAdded = null;
				} else {
					GasStack toWrite = new GasStack((Gas) notAdded.getGas(), freeBytes());
					currentGases.set(i, toWrite);
					if (mode == Actionable.MODULATE) {
						writeGasToSlot(i, toWrite);
					}
					notAdded.setStackSize(notAdded.getStackSize() - freeBytes());
				}
			}
		}
		if (notAdded == null || !notAdded.equals(input)) {
			requestSave();
		}
		return notAdded;
	}

	@Override
	public boolean isFormatted() {
		// Common case
		if (this.prioritizedGases.isEmpty()) {
			return false;
		}

		for (Gas currentGas : this.prioritizedGases) {
			if (currentGas != null) {
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean isPrioritized(IAEGasStack input) {
		return input != null
			&& this.prioritizedGases.contains(input.getGas());
	}

	private void requestSave() {
		if (this.saveProvider != null) {
			this.saveProvider.saveChanges(this);
		}
	}

	@Override
	public int totalBytes() {
		return this.totalBytes / 250; // HACK HACK HACK pending rework
	}

	@Override
	public int totalTypes() {
		return this.totalTypes;
	}

	@Override
	public int usedBytes() {
		return (this.totalBytes - freeBytes()) / 250; // HACK HACK HACK pending rework
	}

	@Override
	public int usedTypes() {
		int i = 0;
		for (GasStack stack : this.gasStacks) {
			if (stack != null) {
				i++;
			}
		}
		return i;
	}

	@Override
	public long storedCount() {
		return this.totalBytes - freeBytes();
	}

	@Override
	public boolean validForPass(int i) {
		return true; // TODO
	}

	protected void writeGasToSlot(int i, GasStack gasStack) {
		NBTTagCompound gasTag = new NBTTagCompound();
		if (gasStack != null && gasStack.amount > 0) {
			gasTag = gasStack.write(gasTag);
			this.stackTag.setTag("Gas#" + i, gasTag);
		} else {
			this.stackTag.removeTag("Gas#" + i);
		}
		this.gasStacks.set(i, gasStack);
		//Removes old Fluid Tag
		this.stackTag.removeTag("Fluid#" + i);

	}

	@Override
	public ICellInventory<IAEGasStack> getCellInv() {
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
		return 8;
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
		return this.freeBytes();
	}

	@Override
	public long getUsedBytes() {
		return this.usedBytes();
	}

	@Override
	public long getTotalItemTypes() {
		return this.totalTypes();
	}

	@Override
	public long getStoredItemCount() {
		return this.storedCount();
	}

	@Override
	public long getStoredItemTypes() {
		//return 0;
		return this.usedTypes();
	}

	@Override
	public long getRemainingItemTypes() {
		//return 0;
		return this.totalTypes() - this.usedTypes();
	}

	@Override
	public long getRemainingItemCount() {
		return 0;
		//return this.total
	}

	@Override
	public int getUnusedItemCount() {
		return 0;
		//return this.fre
	}

	@Override
	public int getStatusForCell() {
		if( this.canHoldNewItem() )
			return 1;
		if( this.getRemainingItemCount() > 0 )
			return 2;
		return 3;
	}

	@Override
	public void persist() {
		if (this.saveProvider != null) {
			this.saveProvider.saveChanges(this);
		}
	}
}
