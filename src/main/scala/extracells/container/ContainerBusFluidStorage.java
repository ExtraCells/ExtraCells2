package extracells.container;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import extracells.container.slot.SlotNetworkTool;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiBusFluidStorage;
import extracells.part.PartFluidStorage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBusFluidStorage extends Container {

	private GuiBusFluidStorage guiBusFluidStorage;

	public PartFluidStorage part;

	public ContainerBusFluidStorage(PartFluidStorage part, EntityPlayer player) {

		addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), 0, 187, 8));
		this.part = part;
		bindPlayerInventory(player.inventory);

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(stack)) {
				DimensionalCoord coord = part.getHost().getLocation();
				IGuiItem guiItem = (IGuiItem) stack.getItem();
				INetworkTool networkTool = (INetworkTool) guiItem.getGuiObject(stack, coord.getWorld(), coord.x, coord.y, coord.z);
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 3; k++) {
						addSlotToContainer(new SlotNetworkTool(networkTool, j + k * 3, 187 + k * 18, j * 18 + 102));
					}
				}
				return;
			}
		}
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

	public void setGui(GuiBusFluidStorage _guiBusFluidStorage) {
		this.guiBusFluidStorage = _guiBusFluidStorage;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		if (this.guiBusFluidStorage != null)
			this.guiBusFluidStorage.shiftClick(getSlot(slotnumber).getStack());

		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotnumber < 36) {
				if (!mergeItemStack(itemstack1, 36, this.inventorySlots.size(), true)) {
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 0, 36, false)) {
				return null;
			}

			if (itemstack1.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}

		return itemstack;
	}
}
