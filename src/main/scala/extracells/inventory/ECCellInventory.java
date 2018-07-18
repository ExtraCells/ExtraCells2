package extracells.inventory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.items.IItemHandler;

public class ECCellInventory implements IInventory {

	private ItemStack storage;
	private String tagId;
	private NBTTagCompound tagCompound;
	private int size;
	private int stackLimit;
	private ItemStack[] slots;
	private boolean dirty = false;

	public ECCellInventory(ItemStack storage, String tagId, int size, int stackLimit) {
		this.storage = storage;
		this.tagId = tagId;
		this.size = size;
		this.stackLimit = stackLimit;

		slots = new ItemStack[size];
		for(int i = 0; i < slots.length; i++){
			slots[i] = ItemStack.EMPTY;
		}

		if (!this.storage.hasTagCompound()) {
			this.storage.setTagCompound(new NBTTagCompound());
		}
		this.storage.getTagCompound().setTag(this.tagId, this.storage.getTagCompound().getCompoundTag(this.tagId));
		this.tagCompound = this.storage.getTagCompound().getCompoundTag(this.tagId);
		openInventory();
	}

	@Override
	public void closeInventory(EntityPlayer player) {
		closeInventory();
	}

	private void closeInventory() {
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
		ItemStack itemStack = ItemStackHelper.getAndSplit(Arrays.asList(slots), slotId, amount);

		if (itemStack != null && !itemStack.isEmpty()) {
			this.markDirty();
		}

		return itemStack;
	}

	@Override
	public String getName() {
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
	public boolean isEmpty() {
		for (int i = 0; i < slots.length; i++){
			if(slots[i] != null && !slots[i].isEmpty())
				return true;
		}
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int slotId) {
		return this.slots[slotId];
	}

	@Nullable
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStackHelper.getAndRemove(Arrays.asList(slots), index);
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}


	@Override
	public boolean isItemValidForSlot(int slotId, ItemStack itemStack) {
		return true;
	}

	@Override
	public void markDirty() {
		this.dirty = true;
		closeInventory();
		this.dirty = false;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return false;
	}

	@Override
	public void openInventory(EntityPlayer player) {
		this.slots = new ItemStack[this.size];
		for (int i = 0; i < this.slots.length; i++) {
			this.slots[i] = new ItemStack(this.tagCompound
				.getCompoundTag("ItemStack#" + i));
		}
	}

	private void openInventory() {
		this.slots = new ItemStack[this.size];
		for (int i = 0; i < this.slots.length; i++) {
			this.slots[i] = new ItemStack(this.tagCompound
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
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Nonnull
	public ItemStack extractItem(int slot, int amount, boolean simulate) {
		if (isSlotEmpty(slot))
			return ItemStack.EMPTY;
		ItemStack stack = slots[slot].copy();
		if(amount >= stack.getCount()){
			if(!simulate)
				slots[slot] = null;
			return stack;
		}else{
			stack.setCount(amount);
			if(!simulate)
				slots[slot].setCount(slots[slot].getCount() - amount);
			return stack;
		}
	}

	public int getSlotLimit(int slot) {
		return slots[slot] == null || slots[slot].isEmpty() ? 64 : slots[slot].getMaxStackSize();
	}

	@Nonnull
	public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
		if (stack == null)
			return ItemStack.EMPTY;
		if (!isItemValidForSlot(slot, stack))
			return ItemStack.EMPTY;
		if (isSlotEmpty(slot))
		{
			if (!simulate)
				slots[slot] = stack;
			return stack;
		}else{
			ItemStack oldStack = slots[slot];
			if ((!ItemStack.areItemStackTagsEqual(stack, oldStack)) && oldStack.getMaxStackSize() > oldStack.getCount()){
				ItemStack newStack = stack.copy();
				newStack.setCount(Math.min(newStack.getCount(), oldStack.getMaxStackSize() - oldStack.getCount()));
				if(!simulate)
					oldStack.setCount(oldStack.getCount() + newStack.getCount());
				return newStack;
			}else
				return ItemStack.EMPTY;
		}
	}

	private boolean isSlotEmpty(int slot){
		return slots[slot] == null || slots[slot].isEmpty();
	}

	public int getSlots() {
		return slots.length;
	}
}
