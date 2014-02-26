package extracells.container;

import extracells.container.slot.SlotRespective;
import extracells.part.PartDrive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerDrive extends Container
{

	public ContainerDrive(PartDrive part, EntityPlayer player)
	{

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				addSlotToContainer(new SlotRespective(part.getInventory(), j + i * 3, i * 18 + 71, j * 18 - 4));
			}
		}
		bindPlayerInventory(player.inventory);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 63));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 121));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotnumber < 36)
			{
				if (!mergeItemStack(itemstack1, 36, inventorySlots.size(), true))
				{
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 0, 36, false))
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

		return itemstack;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}
}
