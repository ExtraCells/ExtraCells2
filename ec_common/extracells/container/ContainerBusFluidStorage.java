package extracells.container;

import extracells.slot.SlotFake;
import extracells.slot.SlotRespective;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class ContainerBusFluidStorage extends Container
{
	public ContainerBusFluidStorage(IInventory inventoryPlayer, IInventory inventoryTileEntity)
	{
		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryTileEntity, j + i * 9, 8 + j * 18, i * 18 - 10)
				{
					public boolean isItemValid(ItemStack itemstack)
					{
						return FluidContainerRegistry.isFilledContainer(itemstack);
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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 112));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 170));
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
			if (FluidContainerRegistry.isContainer(itemstack))
			{
				if (slotnumber >= 0 && slotnumber <= 53)
				{
					if (!mergeItemStack(itemstack1, 54, 90, false))
						return null;
				} else if (slotnumber >= 54 && slotnumber <= 90)
				{
					if (!mergeItemStack(itemstack1, 0, 53, false))
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