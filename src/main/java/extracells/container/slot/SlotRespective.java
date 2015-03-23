package extracells.container.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotRespective extends Slot {

	IInventory inventory;

	public SlotRespective(IInventory inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.inventory = inventory;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack) {
		return this.inventory.isItemValidForSlot(this.slotNumber, itemstack);
	}
}
