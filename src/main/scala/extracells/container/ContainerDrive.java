package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import extracells.container.slot.SlotRespective;
import extracells.part.PartDrive;

public class ContainerDrive extends Container {
	PartDrive part;

	public ContainerDrive(PartDrive part, EntityPlayer player) {
		this.part = part;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				addSlotToContainer(new SlotRespective(part.getInventory(), i
					+ (j * (-1) + 1) * 3, 18 + 71 - (j * (-1) + 1) * 18, i * 18 - 4));
			}
		}
		bindPlayerInventory(player.inventory);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
					8 + j * 18, i * 18 + 63));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 121));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return part.isValid();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack transferStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			transferStack = itemStack.copy();

			if (slotnumber < 6) {
				if (!mergeItemStack(itemStack, 6, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotnumber < 33){
				if (!mergeItemStack(itemStack, 0, 6, false) &&
						!mergeItemStack(itemStack, 33, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemStack, 0, 6, false) &&
					!mergeItemStack(itemStack, 6, 33, false)) {
				return ItemStack.EMPTY;
			}

			if (itemStack.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return transferStack;
	}
}
