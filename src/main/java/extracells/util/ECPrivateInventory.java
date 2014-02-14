package extracells.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class ECPrivateInventory implements IInventory
{
	public ItemStack[] slots;
	public String customName;
	private int stackLimit;
	private IInventoryUpdateReceiver receiver;

	public ECPrivateInventory(String _customName, int _size, int _stackLimit)
	{
		this(_customName, _size, _stackLimit, null);
	}

	public ECPrivateInventory(String _customName, int _size, int _stackLimit, IInventoryUpdateReceiver _receiver)
	{
		slots = new ItemStack[_size];
		customName = _customName;
		stackLimit = _stackLimit;
		receiver = _receiver;
	}

	@Override
	public int getSizeInventory()
	{
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return slots[i];
	}

	@Override
	public ItemStack decrStackSize(int slotId, int amount)
	{
		if (slots[slotId] == null)
			return null;
		ItemStack itemstack;
		if (slots[slotId].stackSize <= amount)
		{
			itemstack = slots[slotId];
			slots[slotId] = null;
			markDirty();
			return itemstack;
		} else
		{
			ItemStack temp = slots[slotId];
			itemstack = temp.splitStack(amount);
			slots[slotId] = temp;
			if (temp.stackSize == 0)
			{
				slots[slotId] = null;
			} else
			{
				slots[slotId] = temp;
			}
			markDirty();
			return itemstack;
		}
	}

	/**
	 * Increases the stack size of a slot.
	 * 
	 * @param slotId ID of the slot
	 * @param amount amount to be drained
	 * @return the added Stack
	 */
	public ItemStack incrStackSize(int slotId, int amount)
	{
		ItemStack slot = slots[slotId];
		if (slot == null)
			return null;
		int stackLimit = getInventoryStackLimit();
		if (stackLimit > slot.getMaxStackSize())
			stackLimit = slot.getMaxStackSize();
		ItemStack added = slot.copy();
		added.stackSize = slot.stackSize + amount > stackLimit ? stackLimit : amount;
		slot.stackSize += added.stackSize;
		return added;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId)
	{
		return slots[slotId];
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack)
	{
		if (itemstack != null && itemstack.stackSize > getInventoryStackLimit())
		{
			itemstack.stackSize = getInventoryStackLimit();
		}
		slots[slotId] = itemstack;

		markDirty();
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public String getInventoryName()
	{
		return customName;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return stackLimit;
	}

	@Override
	public void markDirty()
	{
		if (receiver != null)
			receiver.onInventoryChanged();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openInventory()
	{
		// NOBODY needs this!
	}

	@Override
	public void closeInventory()
	{
		// NOBODY needs this!
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	public void readFromNBT(NBTTagList nbtList)
	{
		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = nbtList.getCompoundTagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < slots.length)
			{
				slots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound);
			}
		}
	}

	public NBTTagList writeToNBT()
	{
		NBTTagList nbtList = new NBTTagList();

		for (int i = 0; i < slots.length; ++i)
		{
			if (slots[i] != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				slots[i].writeToNBT(nbttagcompound);
				nbtList.appendTag(nbttagcompound);
			}
		}
		return nbtList;
	}

	public interface IInventoryUpdateReceiver
	{
		public void onInventoryChanged();
	}
}