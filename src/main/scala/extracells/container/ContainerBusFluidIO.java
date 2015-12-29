package extracells.container;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import extracells.container.slot.SlotNetworkTool;
import extracells.container.slot.SlotRespective;
import extracells.gui.GuiBusFluidIO;
import extracells.part.PartFluidIO;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBusFluidIO extends Container {
	private PartFluidIO part;
	private GuiBusFluidIO guiBusFluidIO;

	public ContainerBusFluidIO(PartFluidIO part, EntityPlayer player) {
		this.part = part;
		for (int i = 0; i < 4; i++)
			addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), i, 187, i * 18 + 8));
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
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 102));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 160));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return part.isValid();
	}

	@Override
	protected void retrySlotClick(int par1, int par2, boolean par3, EntityPlayer par4EntityPlayer) {
		// NOPE
	}

	public void setGui(GuiBusFluidIO _guiBusFluidIO) {
		this.guiBusFluidIO = _guiBusFluidIO;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		if (this.guiBusFluidIO != null && this.guiBusFluidIO.shiftClick(getSlot(slotnumber).getStack()))
			return ((Slot) this.inventorySlots.get(slotnumber)).getStack();

		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();

			if (slotnumber < 36) {
				if (!mergeItemStack(itemstack1, 36, this.inventorySlots.size(),
						true)) {
					return null;
				}
			} else if (!mergeItemStack(itemstack1, 0, 36, false)) {
				return itemstack1;
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
