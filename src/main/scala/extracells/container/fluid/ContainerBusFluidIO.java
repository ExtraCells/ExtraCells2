package extracells.container.fluid;

import extracells.gui.IFluidSlotGuiTransfer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import extracells.container.ContainerUpgradeable;
import extracells.part.fluid.PartFluidIO;

public class ContainerBusFluidIO extends ContainerUpgradeable {
	private PartFluidIO part;
	private IFluidSlotGuiTransfer guiBusFluidIO;

	public ContainerBusFluidIO(PartFluidIO part, EntityPlayer player) {
		this.part = part;
		bindPlayerInventory(player.inventory, 8, 102);
		bindUpgradeInventory(player.inventory, part);
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return part.isValid();
	}

//	@Override
//	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer) {
//		// NOPE
//	}

	public void setGui(IFluidSlotGuiTransfer _guiBusFluidIO) {
		this.guiBusFluidIO = _guiBusFluidIO;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		//TODO:remove the gui from this
		if (this.guiBusFluidIO != null && this.guiBusFluidIO.shiftClick(getSlot(slotnumber).getStack())) {
			return ItemStack.EMPTY;
		}

		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotnumber < 36) {
				if (!mergeItemStack(itemstack1, 36, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(itemstack1, 0, 36, false)) {
				return itemstack1;
			}

			if (itemstack1.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
}
