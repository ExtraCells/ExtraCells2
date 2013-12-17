package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import extracells.container.slot.SlotRespective;
import extracells.tile.TileEntityHardMEDrive;

public class ContainerHardMEDrive extends Container
{

	IInventory tileentity;

	public ContainerHardMEDrive(InventoryPlayer inventory, TileEntityHardMEDrive tileentity)
	{
		this.tileentity = tileentity;
		for (int i = 0; i < 3; i++)
		{
			addSlotToContainer(new SlotRespective(tileentity, i, 80, 17 + i * 18)
			{
				public boolean isItemValid(ItemStack item)
				{
					return appeng.api.Util.getCellRegistry().isCellHandled(item);
				}
			});
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
			if (appeng.api.Util.getCellRegistry().isCellHandled(itemstack))
			{
				if (i < 3)
				{
					if (!mergeItemStack(itemstack1, 3, 38, false))
					{
						return null;
					}
				} else if (!mergeItemStack(itemstack1, 0, 3, false))
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

	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer)
	{
		// DONT DO ANYTHING YOU SHITTY METHOD!
	}
}
