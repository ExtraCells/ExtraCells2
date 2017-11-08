package extracells.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

import extracells.util.ItemStackUtils;
import net.minecraftforge.items.IItemHandler;

import java.util.Arrays;

public class InventoryPlain implements IInventory {

	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	@Nullable
	private IInventoryListener listener;

	public InventoryPlain(String customName, int size, int stackLimit) {
		this(customName, size, stackLimit, null);
	}

	public InventoryPlain(String customName, int size, int stackLimit, IInventoryListener listener) {
		this.slots = new ItemStack[size];
		this.customName = customName;
		this.stackLimit = stackLimit;
		this.listener = listener;
		for(int i = 0; i < slots.length; i++){
			slots[i] = ItemStack.EMPTY;
		}
	}

	@Override
	public ItemStack decrStackSize(int slotId, int amount) {
		ItemStack itemStack = ItemStackHelper.getAndSplit(Arrays.asList(slots), slotId, amount);

		if (itemStack != null && !itemStack.isEmpty()) {
			this.markDirty();
			onContentsChanged();
		}

		return itemStack;
	}

	@Override
	public int getInventoryStackLimit() {
		return this.stackLimit;
	}

	@Override
	public int getSizeInventory() {
		return this.slots.length;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < slots.length; i++){
			if(slots[i] != null && !slots[i].isEmpty())
				return true;
		}
		return false;
	}

	public int getSlots() {
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return this.slots[i];
	}

	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack == null)
			return ItemStack.EMPTY;
		if (!isItemValidForSlot(slot, stack))
			return ItemStack.EMPTY;
		if (isSlotEmpty(slot))
		{
			if (!simulate) {
				slots[slot] = stack;
				onContentsChanged();
			}
			return stack;
		}else{
			ItemStack oldStack = slots[slot];
			if ((!ItemStack.areItemStackTagsEqual(stack, oldStack)) && oldStack.getMaxStackSize() > oldStack.getCount()){
				ItemStack newStack = stack.copy();
				newStack.setCount(Math.min(newStack.getCount(), oldStack.getMaxStackSize() - oldStack.getCount()));
				if(!simulate) {
					oldStack.setCount(oldStack.getCount() + newStack.getCount());
					onContentsChanged();
				}
				return newStack;
			}else
				return ItemStack.EMPTY;
		}
	}

	private boolean isSlotEmpty(int slot){
		return slots[slot] == null || slots[slot].isEmpty();
	}

	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (isSlotEmpty(slot))
			return ItemStack.EMPTY;
		ItemStack stack = slots[slot].copy();
		if(amount >= stack.getCount()){
			if(!simulate){
				slots[slot] = ItemStack.EMPTY;
				onContentsChanged();
			}
			return stack;
		}else{
			stack.setCount(amount);
			if(!simulate) {
				slots[slot].setCount(slots[slot].getCount() - amount);
				onContentsChanged();
			}
			return stack;
		}
	}

	public int getSlotLimit(int slot) {
		return slots[slot] == null || slots[slot].isEmpty() ? 64 : slots[slot].getMaxStackSize();
	}

	@Nullable
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(Arrays.asList(slots), index);
	}

	@Override
	public String getName() {
		return this.customName;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	/**
	 * Increases the stack size of a slot.
	 *
	 * @param slotId ID of the slot
	 * @param amount amount to be drained
	 * @return the added Stack
	 */
	public ItemStack incrStackSize(int slotId, int amount) {
		ItemStack slot = this.slots[slotId];
		if (ItemStackUtils.isEmpty(slot)) {
			return ItemStack.EMPTY;
		}
		int stackLimit = getInventoryStackLimit();
		if (stackLimit > slot.getMaxStackSize()) {
			stackLimit = slot.getMaxStackSize();
		}
		ItemStack added = slot.copy();
		added.setCount(slot.getCount() + amount > stackLimit ? stackLimit : amount);
		slot.setCount(slot.getCount() +  added.getCount());
		onContentsChanged();
		return added;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public void markDirty() {
		if (this.listener != null) {
			this.listener.onInventoryChanged();
		}
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		// NOBODY needs this!
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		// NOBODY needs this!
	}

	public void readFromNBT(NBTTagList nbtList) {
		if (nbtList == null) {
			for (int i = 0; i < slots.length; i++) {
				slots[i] = ItemStack.EMPTY;
			}
			return;
		}
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbtList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < this.slots.length) {
				this.slots[j] = new ItemStack(nbttagcompound);
			}
		}
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (stack != null && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
		}
		this.slots[index] = stack;
		onContentsChanged();
		markDirty();
	}

	public NBTTagList writeToNBT() {
		NBTTagList nbtList = new NBTTagList();

		for (int i = 0; i < this.slots.length; ++i) {
			if (this.slots[i] != null) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				this.slots[i].writeToNBT(nbttagcompound);
				nbtList.appendTag(nbttagcompound);
			}
		}
		return nbtList;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public void clear() {
		for (int i = 0; i < slots.length; i++) {
			slots[i] = ItemStack.EMPTY;
		}
		onContentsChanged();
	}

	protected void onContentsChanged() {
	}
}
