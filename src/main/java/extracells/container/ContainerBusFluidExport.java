package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import extracells.container.slot.SlotFake;

public class ContainerBusFluidExport extends ECContainer
{
	IInventory tileentity;

	public ContainerBusFluidExport(IInventory inventoryPlayer, IInventory inventoryTileEntity)
	{
		super(inventoryTileEntity.getSizeInventory());

		tileentity = inventoryTileEntity;

		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				addSlotToContainer(new SlotFake(inventoryTileEntity, j + i * 4, 53 + j * 18, i * 18 + 21)
				{
					public boolean isItemValid(ItemStack itemstack)
					{
						return tileentity.isItemValidForSlot(0, itemstack);
					}
				});
			}
		}

		bindPlayerInventory(inventoryPlayer);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 79));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 137));
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
			itemstack.stackSize = 1;
			if (tileentity.isItemValidForSlot(0, itemstack1))
			{
				if (slotnumber >= 0 && slotnumber <= 7)
				{
					shiftItemStack(itemstack, 8, 43);
					return null;
				} else if (slotnumber >= 8 && slotnumber <= 43)
				{
					shiftItemStack(itemstack, 0, 7);
					return null;
				}
				if (itemstack1.stackSize == 0)
				{
					slot.putStack(null);
				} else
				{
					slot.onSlotChanged();
				}
			} else
			{
				return null;
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
