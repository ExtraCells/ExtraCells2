package extracells.models.drive;

import appeng.api.storage.ICellContainer;

public interface IDrive extends ICellContainer {
	int getCellCount();

	/**
	 * 0 - cell is missing.
	 *
	 * 1 - green,
	 *
	 * 2 - orange,
	 *
	 * 3 - red
	 *
	 * @param index slot index
	 * @return status of the slot, one of the above indices.
	 */
	int getCellStatus(int index);

	boolean isPowered();
}
