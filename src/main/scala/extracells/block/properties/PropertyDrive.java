package extracells.block.properties;

import net.minecraftforge.common.property.IUnlistedProperty;

import extracells.models.drive.DriveSlotsState;

public class PropertyDrive implements IUnlistedProperty<DriveSlotsState> {
	public static final PropertyDrive INSTANCE = new PropertyDrive("drive");

	private final String name;

	public PropertyDrive(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public Class<DriveSlotsState> getType() {
		return DriveSlotsState.class;
	}

	@Override
	public boolean isValid(DriveSlotsState value) {
		return value != null;
	}

	@Override
	public String valueToString(DriveSlotsState value) {
		return value.toString();
	}
}
