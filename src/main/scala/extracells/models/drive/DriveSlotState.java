package extracells.models.drive;

import net.minecraft.util.IStringSerializable;

public enum DriveSlotState implements IStringSerializable {

	// No cell in slot
	EMPTY("empty"),

	// Cell in slot, but unpowered
	OFFLINE("offline"),

	// Online and free space
	ONLINE("online"),

	// Types full, space left
	TYPES_FULL("types_full"),

	// Completely full
	FULL("full");

	private final String name;

	DriveSlotState(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	public static DriveSlotState fromCellStatus(int cellStatus) {
		switch (cellStatus) {
			default:
			case 0:
				return DriveSlotState.EMPTY;
			case 1:
				return DriveSlotState.ONLINE;
			case 2:
				return DriveSlotState.TYPES_FULL;
			case 3:
				return DriveSlotState.FULL;
		}
	}

}
