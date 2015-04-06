package extracells.util.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ECPrivateInventory implements IInventory {

	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	private IInventoryUpdateReceiver receiver;

	public ECPrivateInventory(String _customName, int _size, int _stackLimit) {
		this(_customName, _size, _stackLimit, null);
	}

	public ECPrivateInventory(String _customName, int _size, int _stackLimit,
			IInventoryUpdateReceiver _receiver) {
		this.slots = new ItemStack[_size];
		this.customName = _customName;
		this.stackLimit = _stackLimit;
		this.receiver = _receiver;
	}

	@Override
	public void closeInventory() {
		// NOBODY needs this!
	}

	@Override
	public ItemStack decrStackSize(int slotId, int amount) {
		if (this.slots[slotId] == null)
			return null;
		ItemStack itemstack;
		if (this.slots[slotId].stackSize <= amount) {
			itemstack = this.slots[slotId];
			this.slots[slotId] = null;
			markDirty();
			return itemstack;
		} else {
			ItemStack temp = this.slots[slotId];
			itemstack = temp.splitStack(amount);
			this.slots[slotId] = temp;
			if (temp.stackSize == 0) {
				this.slots[slotId] = null;
			} else {
				this.slots[slotId] = temp;
			}
			markDirty();
			return itemstack;
		}
	}

	@Override
	public String getInventoryName() {
		return this.customName;
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
	public ItemStack getStackInSlot(int i) {
		return this.slots[i];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return this.slots[slotId];
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	/**
	 * Increases the stack size of a slot.
	 *
	 * @param slotId
	 *        ID of the slot
	 * @param amount
	 *        amount to be drained
	 *
	 * @return the added Stack
	 */
	public ItemStack incrStackSize(int slotId, int amount) {
		ItemStack slot = this.slots[slotId];
		if (slot == null)
			return null;
		int stackLimit = getInventoryStackLimit();
		if (stackLimit > slot.getMaxStackSize())
			stackLimit = slot.getMaxStackSize();
		ItemStack added = slot.copy();
		added.stackSize = slot.stackSize + amount > stackLimit ? stackLimit
				: amount;
		slot.stackSize += added.stackSize;
		return added;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void markDirty() {
		if (this.receiver != null)
			this.receiver.onInventoryChanged();
	}

	@Override
	public void openInventory() {
		// NOBODY needs this!
	}

	public void readFromNBT(NBTTagList nbtList) {
		if(nbtList == null){
			for(int i = 0; i < slots.length; i++){
				slots[i] = null;
			}
			return;
		}
		for (int i = 0; i < nbtList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = nbtList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < this.slots.length) {
				this.slots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit()) {
			itemstack.stackSize = getInventoryStackLimit();
		}
		this.slots[slotId] = itemstack;

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
}
