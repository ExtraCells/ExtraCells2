package extracells.container;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IItemDefinition;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.util.DimensionalCoord;
import extracells.container.slot.SlotNetworkTool;
import extracells.container.slot.SlotRespective;

public abstract class ContainerUpgradeable extends ContainerBase {
	public ContainerUpgradeable() {
	}

	public void bindUpgradeInventory(InventoryPlayer inv, IUpgradeable upgradeable) {
		IInventory upgradeInventory = upgradeable.getUpgradeInventory();
		for (int i = 0; i < upgradeInventory.getSizeInventory(); i++) {
			addSlotToContainer(new SlotRespective(upgradeInventory, i, 187, i * 18 + 8));
		}

		IItemDefinition networkToolDefinition = AEApi.instance().definitions().items().networkTool();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack != null && networkToolDefinition.isSameAs(stack)) {
				lockPlayerInventorySlot(i);
				DimensionalCoord coord = upgradeable.getLocation();
				IGuiItem guiItem = (IGuiItem) stack.getItem();
				INetworkTool networkTool = (INetworkTool) guiItem.getGuiObject(stack, coord.getWorld(), coord.getPos());
				for (int j = 0; j < 3; j++) {
					for (int k = 0; k < 3; k++) {
						addSlotToContainer(new SlotNetworkTool(networkTool, k + j * 3, 187 + k * 18, j * 18 + 102));
					}
				}
				return;
			}
		}
	}

}
