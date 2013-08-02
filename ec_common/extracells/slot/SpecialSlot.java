package extracells.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SpecialSlot extends Slot
{
	IInventory inventory;

	public SpecialSlot(IInventory inventory, int x, int y, int z)
	{
		super(inventory, x, y, z);
		this.inventory = inventory;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		return inventory.isItemValidForSlot(this.slotNumber, itemstack);
	}
}
