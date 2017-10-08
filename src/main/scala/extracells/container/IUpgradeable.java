package extracells.container;

import net.minecraft.inventory.IInventory;

import appeng.api.util.DimensionalCoord;

public interface IUpgradeable {

	DimensionalCoord getLocation();

	IInventory getUpgradeInventory();
}
