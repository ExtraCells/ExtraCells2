package extracells.container.fluid;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import extracells.container.slot.SlotRespective;

public class ContainerFluidCrafter extends Container {
	IInventory tileentity;

	public ContainerFluidCrafter(InventoryPlayer player, IInventory tileentity) {
		this.tileentity = tileentity;
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new SlotRespective(tileentity, j + i * 3,
					62 + j * 18, 17 + i * 18));
			}
		}
		bindPlayerInventory(player);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
					8 + j * 18, i * 18 + 84));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer) {
		super.onContainerClosed(entityplayer);
	}

//	@Override
//	protected void retrySlotClick(int par1, int par2, boolean par3,
//		EntityPlayer par4EntityPlayer) {
//		// DON'T DO ANYTHING, YOU SHITTY METHOD!
//	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack transferStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemStack = slot.getStack();
			transferStack = itemStack.copy();

			if (slotnumber < 9) {
				if (!mergeItemStack(itemStack, 9, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (slotnumber < 36){
				if (!mergeItemStack(itemStack, 0, 9, false) &&
						!mergeItemStack(itemStack, 36, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemStack, 0, 9, false) &&
					!mergeItemStack(itemStack, 9, 36, false)) {
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
