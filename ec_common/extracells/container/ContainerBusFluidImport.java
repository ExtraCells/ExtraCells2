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

public class ContainerBusFluidImport extends Container
{
	public ContainerBusFluidImport(IInventory inventoryPlayer, IInventory inventoryTileEntity)
	{
		for (int i = 0; i < 2; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				addSlotToContainer(new Slot(inventoryTileEntity, j + i * 4, 53 + j * 18, i * 18 + 20)
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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 78));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 136));
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
			if (FluidContainerRegistry.isFilledContainer(itemstack))
			{
				if (slotnumber >= 0 && slotnumber <= 7)
				{
					if (!mergeItemStack(itemstack1, 8, 43, false))
						return null;
				} else if (slotnumber >= 8 && slotnumber <= 43)
				{
					if (!mergeItemStack(itemstack1, 0, 7, false))
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