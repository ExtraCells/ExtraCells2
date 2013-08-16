package extracells.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPhantom extends Slot
{

	int invSlot;

	public SlotPhantom(IInventory inv, int idx, int x, int y)
	{
		super(inv, idx, x, y);
		this.invSlot = idx;
	}

	public void addToInv(ItemStack is)
	{
		if (is != null)
		{
			ItemStack current = this.inventory.getStackInSlot(this.invSlot);
			if (current != null)
			{
				current.stackSize += is.stackSize;
				if (current.stackSize > this.inventory.getInventoryStackLimit())
				{
					current.stackSize = this.inventory.getInventoryStackLimit();
				}
			} else
			{
				current = is.copy();
				if (current.stackSize > this.inventory.getInventoryStackLimit())
				{
					current.stackSize = this.inventory.getInventoryStackLimit();
				}

				this.inventory.setInventorySlotContents(this.invSlot, current);
			}
		} else
		{
			this.inventory.setInventorySlotContents(this.invSlot, (ItemStack) null);
		}

	}

	public boolean canTakeStack(EntityPlayer par1EntityPlayer)
	{
		return false;
	}

	public ItemStack decrStackSize(int par1)
	{
		ItemStack current = this.inventory.getStackInSlot(this.invSlot);
		if (current != null)
		{
			--current.stackSize;
			if (current.stackSize <= 0)
			{
				this.inventory.setInventorySlotContents(this.invSlot, (ItemStack) null);
			}
		}

		return null;
	}

	public void onPickupFromSlot(EntityPlayer par1EntityPlayer, ItemStack par2ItemStack)
	{
	}

	public void putStack(ItemStack par1ItemStack)
	{
		this.inventory.setInventorySlotContents(this.invSlot, par1ItemStack);
	}

	public boolean isItemValid(ItemStack par1ItemStack)
	{
		return true;
	}
}
