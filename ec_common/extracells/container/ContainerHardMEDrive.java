package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import extracells.slot.SlotRespective;
import extracells.tile.TileEntityHardMEDrive;

public class ContainerHardMEDrive extends Container
{

	IInventory tileentity;

	public ContainerHardMEDrive(InventoryPlayer inventory, TileEntityHardMEDrive tileentity)
	{
		this.tileentity = tileentity;
		for (int i = 0; i < 3; i++)
		{
			addSlotToContainer(new SlotRespective(tileentity, i, 62 + 18, 17 + i * 18));
		}
		bindPlayerInventory(inventory);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer p, int i)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(i);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (slot.isItemValid(itemstack))
			{
				if (i < 1)
				{
					if (!mergeItemStack(itemstack1, 1, inventorySlots.size(), true))
					{
						return null;
					}
				} else if (!mergeItemStack(itemstack1, 0, 1, false))
				{
					return null;
				}
				if (itemstack1.stackSize == 0)
				{
					slot.putStack(null);
				} else
				{
					slot.onSlotChanged();
				}
			}
		}
		return itemstack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
	}
}
