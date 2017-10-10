package extracells.inventory;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.definitions.IMaterials;

public class UpgradeInventory extends InventoryPlain {
	public UpgradeInventory(IInventoryListener listener) {
		super("", 4, 1, listener);
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		IMaterials materials = AEApi.instance().definitions().materials();
		if (materials.cardCapacity().isSameAs(itemStack)) {
			return true;
		} else if (materials.cardSpeed().isSameAs(itemStack)) {
			return true;
		} else if (materials.cardRedstone().isSameAs(itemStack)) {
			return true;
		}
		return false;
	}
}
