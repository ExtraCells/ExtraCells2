package extracells.inventory;

import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import extracells.part.PartDrive;

public class InventoryPartDrive extends InventoryPlain {
	private final ICellRegistry cellRegistry;

	public InventoryPartDrive(PartDrive listener) {
		super("extracells.part.drive", 6, 1, listener);
		cellRegistry = AEApi.instance().registries().cell();
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemStack) {
		return this.cellRegistry.isCellHandled(itemStack);
	}
}
