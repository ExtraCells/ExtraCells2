package extracells.container.slot;

import extracells.container.IStorageContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotPlayerInventory extends Slot {

	public final IStorageContainer container;

	public SlotPlayerInventory(IInventory arg0,
			IStorageContainer _container, int arg1, int arg2, int arg3) {
		super(arg0, arg1, arg2, arg3);
		this.container = _container;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player) {
		if (player == null || this.container == null)
			return true;
		ItemStack s = player.getCurrentEquippedItem();
		if (s == null || !this.container.hasWirelessTermHandler())
			return true;
		if (s == this.inventory.getStackInSlot(getSlotIndex()))
			return false;
		return true;
	}

}
