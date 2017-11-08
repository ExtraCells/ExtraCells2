package extracells.inventory;

import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import extracells.part.fluid.PartFluidInterface;

public class FluidInterfaceInventory implements IInventory {

	private PartFluidInterface part;
	private ItemStack[] inv = new ItemStack[9];

	public FluidInterfaceInventory(PartFluidInterface part) {
		this.part = part;
		for(int i = 0; i < inv.length; i++){
			inv[i] = ItemStack.EMPTY;
		}
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null && !stack.isEmpty()) {
			if (stack.getCount() <= amt) {
				setInventorySlotContents(slot, ItemStack.EMPTY);
			} else {
				stack = stack.splitStack(amt);
				if (stack.getCount() == 0) {
					setInventorySlotContents(slot, ItemStack.EMPTY);
				}
			}
		}
		onContentsChanged();
		part.markForUpdate();
		return stack;
	}

	public ItemStack[] getInv() {
		return inv;
	}

	@Override
	public String getName() {
		return I18n.translateToLocal("inventory.fluidInterface");
	}

	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public int getSizeInventory() {
		return this.inv.length;
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < inv.length; i++){
			if(inv[i] != null && !inv[i].isEmpty())
				return true;
		}
		return false;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return this.inv[slot];
	}

	@Nullable
	@Override
	public ItemStack removeStackFromSlot(int index) {
		return ItemStack.EMPTY;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		if (stack.getItem() instanceof ICraftingPatternItem) {
			World world = part.getWorld();
			if (world == null) {
				return false;
			}
			ICraftingPatternDetails details = ((ICraftingPatternItem) stack
				.getItem()).getPatternForItem(stack, world);
			return details != null;
		}
		return false;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	public void readFromNBT(NBTTagCompound tagCompound) {

		NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < this.inv.length) {
				this.inv[slot] = new ItemStack(tag);
			}
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		this.inv[slot] = stack;
		if ((stack != null && (!stack.isEmpty())) && stack.getCount() > getInventoryStackLimit()) {
			stack.setCount(getInventoryStackLimit());
			onContentsChanged();
		}
		part.markForUpdate();
	}

	public void writeToNBT(NBTTagCompound tagCompound) {
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < this.inv.length; i++) {
			ItemStack stack = this.inv[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
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

	}

	protected void onContentsChanged(){

	}
}
