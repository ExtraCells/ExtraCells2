package extracells.container.slot;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import appeng.api.implementations.guiobjects.INetworkTool;
import appeng.api.implementations.items.IUpgradeModule;
import net.minecraftforge.items.SlotItemHandler;

public class SlotNetworkTool extends SlotItemHandler {


	public SlotNetworkTool(INetworkTool inventory, int index, int x, int y) {
		super(inventory.getInventory(), index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		Item item = itemStack.getItem();
		if (!(item instanceof IUpgradeModule)) {
			return false;
		}
		IUpgradeModule upgradeModule = (IUpgradeModule) item;
		return upgradeModule.getType(itemStack) != null;
	}
}
