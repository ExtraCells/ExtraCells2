package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import extracells.container.slot.SlotRespective;

public class ContainerTerminalFluid extends Container
{

	IInventory inventoryTileEntity;

	public ContainerTerminalFluid(InventoryPlayer inventory, IInventory inventoryTileEntity)
	{
		this.inventoryTileEntity = inventoryTileEntity;
		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(inventoryTileEntity, 0, 8, 74));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotRespective(inventoryTileEntity, 1, 26, 74)
		{
			public boolean isItemValid(ItemStack itemstack)
			{
				return false;
			}
		});

		bindPlayerInventory(inventory);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 104));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 162));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
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
			if (inventoryTileEntity.isItemValidForSlot(0, itemstack1))
			{
				if (slotnumber == 1 || slotnumber == 0)
				{
					if (!mergeItemStack(itemstack1, 3, 38, false))
						return null;
				} else if (slotnumber != 1 && slotnumber != 0)
				{
					if (!mergeItemStack(itemstack1, 0, 1, false))
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
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
	}
}
