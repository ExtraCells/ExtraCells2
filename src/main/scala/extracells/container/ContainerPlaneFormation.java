package extracells.container;

import extracells.gui.IFluidSlotGuiTransfer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import extracells.container.slot.SlotRespective;
import extracells.part.fluid.PartFluidPlaneFormation;

//TODO:remove the gui from this
public class ContainerPlaneFormation extends ContainerUpgradeable {

	private IFluidSlotGuiTransfer gui;

	public ContainerPlaneFormation(PartFluidPlaneFormation part,
		EntityPlayer player) {
		addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), 0, 187, 8));
		bindPlayerInventory(player.inventory);
		bindUpgradeInventory(player.inventory, part);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
					8 + j * 18, i * 18 + 102));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 160));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void setGui(IFluidSlotGuiTransfer gui) {
		this.gui = gui;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		if (this.gui != null) {
			this.gui.shiftClick(getSlot(slotnumber).getStack());
		}

		ItemStack transferredStack = ItemStack.EMPTY;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			transferredStack = stack.copy();

			if (slotnumber < 36) {
				if (!mergeItemStack(stack, 36, this.inventorySlots.size(), true)) {
					return ItemStack.EMPTY;
				}
			} else if (!mergeItemStack(stack, 0, 36, false)) {
				return ItemStack.EMPTY;
			}

			if (stack.getCount() == 0) {
				slot.putStack(ItemStack.EMPTY);
			} else {
				slot.onSlotChanged();
			}
		}

		return transferredStack;
	}
}
