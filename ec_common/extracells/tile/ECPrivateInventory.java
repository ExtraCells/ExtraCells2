package extracells.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class ECPrivateInventory implements IInventory
{
	ItemStack[] slots;
	String costumName;
	int stackLimit;

	public ECPrivateInventory(ItemStack[] slots, String costumName, int stackLimit)
	{
		this.slots = slots;
		this.costumName = costumName;
		this.stackLimit = stackLimit;
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
		if (this.slots[i] != null)
		{
			ItemStack itemstack;
			if (this.slots[i].stackSize <= j)
			{
				itemstack = this.slots[i];
				this.slots[i] = null;
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				itemstack = this.slots[i].splitStack(j);
				if (this.slots[i].stackSize == 0)
				{
					this.slots[i] = null;
				}
				this.onInventoryChanged();
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
		if (this.slots[i] != null)
		{
			ItemStack itemstack = this.slots[i];
			this.slots[i] = null;
			return itemstack;
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.slots[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return costumName;
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
		return FluidContainerRegistry.isContainer(itemstack);
	}

	@Override
	public void onInventoryChanged()
	{

	}

}
