package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import extracells.part.PartFluidLevelEmitter;

public class ContainerFluidEmitter extends Container
{
	PartFluidLevelEmitter part;
	EntityPlayer player;

	public ContainerFluidEmitter(PartFluidLevelEmitter _part, EntityPlayer _player)
	{
		super();
		part = _part;
		player = _player;
		bindPlayerInventory(player.inventory);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 84));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
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
			if (true)//tileentity.isItemValidForSlot(0, itemstack1))
			{
				if (slotnumber == 0)
				{
					((Slot) inventorySlots.get(0)).putStack(null);
					return null;
				} else if (slotnumber >= 1 && slotnumber <= 36)
				{
					((Slot) inventorySlots.get(0)).putStack(itemstack);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}
}
