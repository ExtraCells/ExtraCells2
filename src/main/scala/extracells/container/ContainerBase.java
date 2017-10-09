package extracells.container;

import java.util.HashSet;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import extracells.container.slot.SlotDisabled;

public abstract class ContainerBase extends Container {
	private final HashSet<Integer> locked = new HashSet<Integer>();

	public ContainerBase() {
	}

	public void lockPlayerInventorySlot(int idx) {
		this.locked.add(idx);
	}

	protected void bindPlayerInventory(InventoryPlayer inv) {
		bindPlayerInventory(inv, 8, 84);
	}

	protected void bindPlayerInventory(InventoryPlayer inv, int offsetX, int offsetY) {
		// bind player inventory
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				if (this.locked.contains(j + i * 9 + 9)) {
					this.addSlotToContainer(new SlotDisabled(inv, j + i * 9 + 9, j * 18 + offsetX, offsetY + i * 18));
				} else {
					this.addSlotToContainer(new Slot(inv, j + i * 9 + 9, j * 18 + offsetX, offsetY + i * 18));
				}
			}
		}

		// bind player hotbar
		for (int i = 0; i < 9; i++) {
			if (this.locked.contains(i)) {
				this.addSlotToContainer(new SlotDisabled(inv, i, i * 18 + offsetX, 58 + offsetY));
			} else {
				this.addSlotToContainer(new Slot(inv, i, i * 18 + offsetX, 58 + offsetY));
			}
		}
	}

}
