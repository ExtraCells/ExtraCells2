package extracells.models;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import appeng.api.AEApi;
import appeng.api.parts.IPartModel;
import appeng.api.parts.IPartModels;
import extracells.Constants;

public enum PartModels implements IPartModel {
	EXPORT_BASE("export_base"),
	EXPORT_HAS_CHANNEL(EXPORT_BASE, "export_has_channel"),
	EXPORT_OFF(EXPORT_BASE, "export_off"),
	EXPORT_ON(EXPORT_BASE, "export_on"),

	IMPORT_BASE("import_base"),
	IMPORT_HAS_CHANNEL(IMPORT_BASE, "import_has_channel"),
	IMPORT_OFF(IMPORT_BASE, "import_off"),
	IMPORT_ON(IMPORT_BASE, "import_on"),

	DISPLAY_BASE("display_base"),
	TERMINAL_OFF(DISPLAY_BASE, "display_status_off", "terminal_off"),
	TERMINAL_ON(DISPLAY_BASE, "display_status_on", "terminal_on"),
	TERMINAL_HAS_CHANNEL(TERMINAL_ON, "display_has_channel"),

	EMITTER_BASE_OFF("emitter_base_off"),
	EMITTER_BASE_ON("emitter_base_on"),
	EMITTER_OFF_OFF(EMITTER_BASE_OFF, "emitter_status_off"),
	EMITTER_OFF_ON(EMITTER_BASE_OFF, "emitter_status_on"),
	EMITTER_OFF_HAS_CHANNEL(EMITTER_BASE_OFF, "emitter_status_has_channel"),
	EMITTER_ON_OFF(EMITTER_BASE_ON, "emitter_status_off"),
	EMITTER_ON_ON(EMITTER_BASE_ON, "emitter_status_on"),
	EMITTER_ON_HAS_CHANNEL(EMITTER_BASE_ON, "emitter_status_has_channel"),

	STORAGE_BUS_BASE("storage_bus"),
	STORAGE_BUS_OFF(STORAGE_BUS_BASE, "storage_bus_off"),
	STORAGE_BUS_ON(STORAGE_BUS_BASE, "storage_bus_on"),
	STORAGE_BUS_HAS_CHANNEL(STORAGE_BUS_BASE, "storage_bus_has_channel"),

	STORAGE_INTERFACE_BASE("storage_interface"),
	STORAGE_INTERFACE_OFF(STORAGE_INTERFACE_BASE, "storage_interface_off"),
	STORAGE_INTERFACE_ON(STORAGE_INTERFACE_BASE, "storage_interface_on"),
	STORAGE_INTERFACE_HAS_CHANNEL(STORAGE_INTERFACE_BASE, "storage_interface_has_channel"),

	PLANE_OFF("plane_off"),
	PLANE_ON("plane_on"),
	PLANE_HAS_CHANNEL("plane_status_has_channel"),

	ANNIHILATION_PLANE_BASE("annihilation_plane_base"),
	ANNIHILATION_PLANE_OFF(ANNIHILATION_PLANE_BASE, PLANE_OFF),
	ANNIHILATION_PLANE_ON(ANNIHILATION_PLANE_BASE, PLANE_OFF),
	ANNIHILATION_PLANE_HAS_CHANNEL(ANNIHILATION_PLANE_BASE, PLANE_HAS_CHANNEL),

	FORMATION_PLANE_BASE("formation_plane_base"),
	FORMATION_PLANE_OFF(FORMATION_PLANE_BASE, PLANE_OFF),
	FORMATION_PLANE_ON(FORMATION_PLANE_BASE, PLANE_OFF),
	FORMATION_PLANE_HAS_CHANNEL(FORMATION_PLANE_BASE, PLANE_HAS_CHANNEL),

	BATTERY_BASE("battery_base"),
	BATTERY_PLANE_OFF(BATTERY_BASE, PLANE_OFF),
	BATTERY_PLANE_ON(BATTERY_BASE, PLANE_OFF),
	BATTERY_PLANE_HAS_CHANNEL(BATTERY_BASE, PLANE_HAS_CHANNEL),

	DRIVE_BASE("drive"),
	DRIVE_ON(DRIVE_BASE, PLANE_ON),
	DRIVE_OFF(DRIVE_BASE, PLANE_OFF),
	DRIVE_HAS_CHANNEL(DRIVE_BASE, PLANE_HAS_CHANNEL),

	MONITOR_BASE("monitor_base"),
	MONITOR_OFF("monitor_off"),
	MONITOR_ON("monitor_on"),
	MONITOR_HAS_CHANNEL("monitor_has_channel"),

	STORAGE_MONITOR_OFF(MONITOR_BASE, MONITOR_OFF, "storage_monitor_off"),
	STORAGE_MONITOR_ON(MONITOR_BASE, MONITOR_ON, "storage_monitor_on"),
	STORAGE_MONITOR_HAS_CHANNEL(MONITOR_BASE, MONITOR_HAS_CHANNEL, "storage_monitor_on"),

	CONVERSION_MONITOR_OFF(MONITOR_BASE, MONITOR_OFF, "conversion_monitor_off"),
	CONVERSION_MONITOR_ON(MONITOR_BASE, MONITOR_ON, "conversion_monitor_on"),
	CONVERSION_MONITOR_HAS_CHANNEL(MONITOR_BASE, MONITOR_HAS_CHANNEL, "conversion_monitor_on");

	boolean requireConnection;
	List<ResourceLocation> locations;

	PartModels(Object... modelNames) {
		this(true, modelNames);
	}

	PartModels(boolean requireConnection, Object... modelNames) {
		this.requireConnection = requireConnection;
		ImmutableList.Builder builder = new ImmutableList.Builder();
		for (Object o : modelNames) {
			if (o instanceof IPartModel) {
				builder.addAll(((IPartModel) o).getModels());
			} else {
				builder.add(new ResourceLocation(Constants.MOD_ID, "part/" + o.toString()));
			}
		}
		locations = builder.build();
	}

	@Override
	public boolean requireCableConnection() {
		return requireConnection;
	}

	@Nonnull
	@Override
	public List<ResourceLocation> getModels() {
		return locations;
	}

	public static void registerModels() {
		IPartModels partModels = AEApi.instance().registries().partModels();
		for (PartModels model : values()) {
			partModels.registerModels(model.getModels());
		}
	}
}
