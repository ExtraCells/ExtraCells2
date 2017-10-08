package extracells.models.drive;

import appeng.api.storage.ICellContainer;

public interface IDrive extends ICellContainer {
	int getCellCount();

	int getCellStatus(int index);

	boolean isPowered();
}
