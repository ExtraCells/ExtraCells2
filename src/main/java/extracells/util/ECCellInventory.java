package extracells.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ECCellInventory implements IInventory
{
	private ItemStack storage;
	private String tagId;
	private NBTTagCompound tagCompound;
	private int size;
	private int stackLimit;
	private ItemStack[] slots;
	private boolean dirty = false;

	public ECCellInventory(ItemStack _storage, String _tagId, int _size, int _stackLimit)
	{
		storage = _storage;
		tagId = _tagId;
		size = _size;
		stackLimit = _stackLimit;
		if (!storage.hasTagCompound())
			storage.setTagCompound(new NBTTagCompound());
		storage.getTagCompound().setTag(tagId, storage.getTagCompound().getCompoundTag(tagId));
		tagCompound = storage.getTagCompound().getCompoundTag(tagId);
		openInventory();
	}

	@Override
	public int getSizeInventory()
	{
		return size;
	}

	@Override
	public ItemStack getStackInSlot(int slotId)
	{
		return slots[slotId];
	}

	@Override
	public ItemStack decrStackSize(int slotId, int amount)
	{
		ItemStack slotContent = slots[slotId];
		if (slotContent == null)
			return null;
		int stackSize = slotContent.stackSize;
		if (stackSize <= 0)
			return null;
		int newAmount;
		if (amount >= stackSize)
		{
			newAmount = stackSize;
			slots[slotId] = null;
		} else
		{
			slots[slotId].stackSize -= amount;
			newAmount = amount;
		}
		ItemStack toReturn = slotContent.copy();
		toReturn.stackSize = amount;
		markDirty();

		return toReturn;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slotId)
	{
		return getStackInSlot(slotId);
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack content)
	{
		ItemStack slotContent = slots[slotId];
		if (slotContent != content)
		{
			slots[slotId] = content;
			markDirty();
		}
	}

	@Override
	public String getInventoryName()
	{
		return "";
	}

	@Override
	public boolean hasCustomInventoryName()
	{
		return false;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return stackLimit;
	}

	@Override
	public void markDirty()
	{
		dirty = true;
		closeInventory();
		dirty = false;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityPlayer)
	{
		return true;
	}

	@Override
	public void openInventory()
	{
		slots = new ItemStack[size];
		for (int i = 0; i < slots.length; i++)
		{
			slots[i] = ItemStack.loadItemStackFromNBT(tagCompound.getCompoundTag("ItemStack#" + i));
		}
	}

	@Override
	public void closeInventory()
	{
		if (dirty)
		{
			for (int i = 0; i < slots.length; i++)
			{
				tagCompound.removeTag("ItemStack#" + i);
				ItemStack content = slots[i];
				if (content != null)
				{
					tagCompound.setTag("ItemStack#" + i, new NBTTagCompound());
					content.writeToNBT(tagCompound.getCompoundTag("ItemStack#" + i));
				}
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int slotId, ItemStack itemStack)
	{
		return true;
	}
}
