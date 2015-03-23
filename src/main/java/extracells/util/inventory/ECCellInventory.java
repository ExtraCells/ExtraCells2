package extracells.util.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ECCellInventory implements IInventory {

	private ItemStack storage;
	private String tagId;
	private NBTTagCompound tagCompound;
	private int size;
	private int stackLimit;
	private ItemStack[] slots;
	private boolean dirty = false;

	public ECCellInventory(ItemStack _storage, String _tagId, int _size,
			int _stackLimit) {
		this.storage = _storage;
		this.tagId = _tagId;
		this.size = _size;
		this.stackLimit = _stackLimit;
		if (!this.storage.hasTagCompound())
			this.storage.setTagCompound(new NBTTagCompound());
		this.storage.getTagCompound().setTag(this.tagId,
				this.storage.getTagCompound().getCompoundTag(this.tagId));
		this.tagCompound = this.storage.getTagCompound().getCompoundTag(
				this.tagId);
		openInventory();
	}

	@Override
	public void closeInventory() {
		if (this.dirty) {
			for (int i = 0; i < this.slots.length; i++) {
				this.tagCompound.removeTag("ItemStack#" + i);
				ItemStack content = this.slots[i];
				if (content != null) {
					this.tagCompound.setTag("ItemStack#" + i,
							new NBTTagCompound());
					content.writeToNBT(this.tagCompound
							.getCompoundTag("ItemStack#" + i));
				}
			}
		}
	}

	@Override
	public ItemStack decrStackSize(int slotId, int amount) {
		ItemStack slotContent = this.slots[slotId];
		if (slotContent == null)
			return null;
		int stackSize = slotContent.stackSize;
		if (stackSize <= 0)
			return null;
		int newAmount;
		if (amount >= stackSize) {
			newAmount = stackSize;
			this.slots[slotId] = null;
		} else {
			this.slots[slotId].stackSize -= amount;
			newAmount = amount;
		}
		ItemStack toReturn = slotContent.copy();
		toReturn.stackSize = amount;
		markDirty();

		return toReturn;
	}

	@Override
	public String getInventoryName() {
		return "";
	}

	@Override
	public int getInventoryStackLimit() {
		return this.stackLimit;
	}

	@Override
	public int getSizeInventory() {
		return this.size;
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return this.slots[slotId];
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId) {
		return getStackInSlot(slotId);
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
		return true;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer) {
		return true;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
		closeInventory();
		this.dirty = false;
	}

	@Override
	public void openInventory() {
		this.slots = new ItemStack[this.size];
		for (int i = 0; i < this.slots.length; i++) {
			this.slots[i] = ItemStack.loadItemStackFromNBT(this.tagCompound
					.getCompoundTag("ItemStack#" + i));
		}
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack content) {
		ItemStack slotContent = this.slots[slotId];
		if (slotContent != content) {
			this.slots[slotId] = content;
			markDirty();
		}
	}
}
