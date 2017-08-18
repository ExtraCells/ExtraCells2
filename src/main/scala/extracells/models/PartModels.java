package extracells.models;

import javax.annotation.Nonnull;
import java.util.List;

import net.minecraft.util.ResourceLocation;

import appeng.api.AEApi;
import appeng.api.parts.IPartModel;
import appeng.api.parts.IPartModels;

public enum PartModels implements IPartModel {
	;
	boolean requireConnection;
	List<ResourceLocation> locations;

	PartModels(String... modelNames) {
		this(true, modelNames);
	}

	PartModels(boolean requireConnection, String... modelNames) {
		this.requireConnection = requireConnection;
		for(String modelName : modelNames){
			locations.add(new ResourceLocation("parts/" + modelName));
		}
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
