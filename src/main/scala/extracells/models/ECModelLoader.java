package extracells.models;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import extracells.Constants;

public class ECModelLoader implements ICustomModelLoader {
	private final Map<String, IModel> builtInModels;

	public ECModelLoader(Map<String, IModel> builtInModels) {
		this.builtInModels = ImmutableMap.copyOf(builtInModels);
	}

	@Override
	public boolean accepts(ResourceLocation modelLocation) {
		if (!modelLocation.getResourceDomain().equals(Constants.MOD_ID)) {
			return false;
		}

		return builtInModels.containsKey(modelLocation.getResourcePath());
	}

	@Override
	public IModel loadModel(ResourceLocation modelLocation) throws Exception {
		return builtInModels.get(modelLocation.getResourcePath());
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		for (IModel model : builtInModels.values()) {
			if (model instanceof IResourceManagerReloadListener) {
				((IResourceManagerReloadListener) model).onResourceManagerReload(resourceManager);
			}
		}
	}
}
