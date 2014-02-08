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
	public ItemStack decrStackSize(int i, int j)
	{
		if (slots[i] != null)
		{
			ItemStack itemstack;
			if (slots[i].stackSize <= j)
			{
				itemstack = slots[i];
				slots[i] = null;
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				ItemStack temp = slots[i];
				itemstack = temp.splitStack(j);
				slots[i] = temp;
				if (temp.stackSize == 0)
				{
					slots[i] = null;
				} else
				{
					slots[i] = temp;
				}
				onInventoryChanged();
				return itemstack;
			}
		} else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (slots[i] != null)
		{
			ItemStack itemstack = slots[i];
			slots[i] = null;
			return itemstack;
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		slots[i] = itemstack;

		onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return customName;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return stackLimit;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
		// NOBODY needs this!
	}

	@Override
	public void closeChest()
	{
		// NOBODY needs this!
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return true;
	}

	@Override
	public void onInventoryChanged()
	{
		if (receiver != null)
			receiver.onInventoryChanged();
	}

	public void readFromNBT(NBTTagList nbtList)
	{
		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound) nbtList.tagAt(i);
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