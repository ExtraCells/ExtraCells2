package extracells.container.slot;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class SlotNetworkTool extends Slot {

	IInventory inventory;

	public SlotNetworkTool(INetworkTool inventory, int index, int x, int y) {
		super(inventory, index, x, y);
		this.inventory = inventory;
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		if (!(item instanceof IUpgradeModule))
			return false;
		IUpgradeModule upgradeModule = (IUpgradeModule) item;
		return upgradeModule.getType(itemStack) != null;
	}
}
