package extracells.inventory;

import net.minecraft.item.ItemStack;

import appeng.api.definitions.IItemDefinition;

public class UpgradeInventorySingle extends InventoryPlain {
	private final IItemDefinition upgradeDefinition;

	public UpgradeInventorySingle(IItemDefinition upgradeDefinition) {
		super("", 1, 1);
		this.upgradeDefinition = upgradeDefinition;
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return upgradeDefinition.isSameAs(itemStack);
	}
}
