package extracells.container.fluid;

import extracells.gui.IFluidSlotGuiTransfer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import extracells.container.ContainerUpgradeable;
import extracells.container.slot.SlotRespective;
import extracells.part.fluid.PartFluidStorage;

public class ContainerBusFluidStorage extends ContainerUpgradeable {

	private IFluidSlotGuiTransfer guiBusFluidStorage;

	public PartFluidStorage part;

	public ContainerBusFluidStorage(PartFluidStorage part, EntityPlayer player) {

		addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), 0, 187, 8));
		this.part = part;
		bindPlayerInventory(player.inventory, 8, 140);
		bindUpgradeInventory(player.inventory, part);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 140));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 198));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return part.isValid();
	}

	public void setGui(IFluidSlotGuiTransfer _guiBusFluidStorage) {
		this.guiBusFluidStorage = _guiBusFluidStorage;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		if (this.guiBusFluidStorage != null) {
			this.guiBusFluidStorage.shiftClick(getSlot(slotnumber).getStack());
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
				return ItemStack.EMPTY;
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
