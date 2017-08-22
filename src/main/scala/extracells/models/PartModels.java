package extracells.models;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
	TERMINAL_HAS_CHANNEL(TERMINAL_ON, "display_has_channel");

	boolean requireConnection;
	List<ResourceLocation> locations;


	PartModels(String... modelNames) {
		this(null, true, modelNames);
	}

	PartModels(PartModels parent, String... modelNames) {
		this(parent, true, modelNames);
	}

	PartModels(@Nullable PartModels parent, boolean requireConnection, String... modelNames) {
		this.requireConnection = requireConnection;
		ImmutableList.Builder builder = new ImmutableList.Builder();
		if(parent != null){
			builder.addAll(parent.getModels());
		}
		for(String modelName : modelNames){
			builder.add(new ResourceLocation(Constants.MOD_ID, "part/" + modelName));
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

	public static void registerModels(){
		IPartModels partModels = AEApi.instance().registries().partModels();
		for(PartModels model : values()){
			partModels.registerModels(model.getModels());
		}
	}
}
