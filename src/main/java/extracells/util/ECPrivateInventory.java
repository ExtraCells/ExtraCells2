package extracells.util;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

public class ECPrivateInventory implements IInventory
{
	public List<ItemStack> slots;
	public String customName;
	private int stackLimit;

	public ECPrivateInventory(String customName, int size, int stackLimit)
	{
		this.slots = Arrays.asList(new ItemStack[size]);
		this.customName = customName;
		this.stackLimit = stackLimit;
	}

	@Override
	public int getSizeInventory()
	{
		return slots.size();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return slots.get(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (slots.get(i) != null)
		{
			ItemStack itemstack;
			if (slots.get(i).stackSize <= j)
			{
				itemstack = slots.get(i);
				slots.set(i, null);
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				ItemStack temp = slots.get(i);
				itemstack = temp.splitStack(j);
				slots.set(i, temp);
				if (temp.stackSize == 0)
				{
					slots.set(i, null);
				} else
				{
					slots.set(i, temp);
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
		if (slots.get(i) != null)
		{
			ItemStack itemstack = slots.get(i);
			slots.set(i, null);
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
		slots.set(i, itemstack);

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
		return FluidContainerRegistry.isContainer(itemstack) || (itemstack != null && itemstack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack) != null);
	}

	@Override
	public void onInventoryChanged()
	{
	}

	public void readFromNBT(NBTTagList nbtList)
	{
		for (int i = 0; i < nbtList.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound = (NBTTagCompound) nbtList.tagAt(i);
			int j = nbttagcompound.getByte("Slot") & 255;

			if (j >= 0 && j < slots.size())
			{
				slots.set(j, ItemStack.loadItemStackFromNBT(nbttagcompound));
			}
		}
	}

	public NBTTagList writeToNBT()
	{
		NBTTagList nbtList = new NBTTagList();

		for (int i = 0; i < slots.size(); ++i)
		{
			if (slots.get(i) != null)
			{
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) i);
				slots.get(i).writeToNBT(nbttagcompound);
				nbtList.appendTag(nbttagcompound);
			}
		}
		return nbtList;
	}
}
