package extracells.container.fluid;

import extracells.util.FluidHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;

import extracells.tileentity.TileEntityFluidFiller;
import net.minecraft.item.ItemStack;

public class ContainerFluidFiller extends Container {
	public TileEntityFluidFiller tileentity;

	public ContainerFluidFiller(InventoryPlayer player,
		TileEntityFluidFiller tileentity) {
		this.tileentity = tileentity;

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

			if (FluidHelper.isEmpty(itemStack)){
				tileentity.containerItem = itemStack.copy();
				tileentity.markDirty();
				return ItemStack.EMPTY;
			} else if (slotnumber < 27) {
				if (!mergeItemStack(itemStack, 27, this.inventorySlots.size(), false)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemStack, 0, 27, false)) {
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
