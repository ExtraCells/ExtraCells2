package extracells.util;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.block.model.ModelBlockDefinition;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.IPerspectiveAwareModel;
import net.minecraftforge.client.model.SimpleModelState;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelUtil {

	private static final Map<ResourceLocation, ModelBlockDefinition> blockDefinitions = Maps.newHashMap();

	public static SimpleModelState loadModelState(ResourceLocation location) {
		return new SimpleModelState(IPerspectiveAwareModel.MapWrapper.getTransforms(loadTransformFromJson(location)));
	}

	private static ItemCameraTransforms loadTransformFromJson(ResourceLocation location) {
		try {
			return ModelBlock.deserialize(getReaderForResource(location)).getAllTransforms();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ItemCameraTransforms.DEFAULT;
	}

	private static Reader getReaderForResource(ResourceLocation location) throws IOException {
		ResourceLocation file = new ResourceLocation(location.getResourceDomain(),
				location.getResourcePath() + ".json");
		IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(file);
		return new BufferedReader(new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8));
	}
}
