package extracells.container;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import extracells.container.slot.SlotNetworkTool;
import extracells.container.slot.SlotRespective;
import extracells.gui.fluid.GuiFluidPlaneFormation;
import extracells.part.fluid.PartFluidPlaneFormation;

public class ContainerPlaneFormation extends Container {

	private GuiFluidPlaneFormation gui;

	public ContainerPlaneFormation(PartFluidPlaneFormation part,
			EntityPlayer player) {
		addSlotToContainer(new SlotRespective(part.getUpgradeInventory(), 0, 187, 8));
		bindPlayerInventory(player.inventory);

		for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if (stack != null && AEApi.instance().definitions().items().networkTool().isSameAs(stack)) {
				DimensionalCoord coord = part.getHost().getLocation();
				IGuiItem guiItem = (IGuiItem) stack.getItem();
				INetworkTool networkTool = (INetworkTool) guiItem.getGuiObject(stack, coord.getWorld(), coord.getPos());
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

	public void setGui(GuiFluidPlaneFormation gui) {
		this.gui = gui;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		if (this.gui != null)
			this.gui.shiftClick(getSlot(slotnumber).getStack());

		ItemStack transferredStack = null;
		Slot slot = this.inventorySlots.get(slotnumber);

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			transferredStack = stack.copy();

			if (slotnumber < 36) {
				if (!mergeItemStack(stack, 36, this.inventorySlots.size(), true)) {
					return null;
				}
			} else if (!mergeItemStack(stack, 0, 36, false)) {
				return null;
			}

			if (stack.stackSize == 0) {
				slot.putStack(null);
			} else {
				slot.onSlotChanged();
			}
		}

		return transferredStack;
	}
}
