package extracells.models.blocks;

import com.google.common.collect.ImmutableList;
import extracells.Constants;
import extracells.models.BlankModel;
import extracells.registries.ItemEnum;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ItemLayerModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class GasItemModel implements IModel {

	public static class ModelLoader implements ICustomModelLoader {
		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			return modelLocation.getNamespace().equals(Constants.MOD_ID)
				&& modelLocation.getPath().contains("models/item/gas/");
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) throws Exception {
			String gasName = modelLocation.getPath().replace("models/item/gas/", "");
			Gas gas = GasRegistry.getGas(gasName);
			if (gas == null) {
				gas = GasRegistry.getRegisteredGasses().iterator().next();
			}
			return new GasItemModel(gas);
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			for (Gas gas : GasRegistry.getRegisteredGasses()) {
				ModelBakery.registerItemVariants(ItemEnum.FLUIDITEM.getItem(), new ResourceLocation(Constants.MOD_ID + ":gas/" + gas.getName()));
			}
		}
	}

	public Gas gas;

	public GasItemModel(Gas gas) {
		this.gas = gas;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Collections.singletonList(gas.getIcon());
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		TextureAtlasSprite sprite = bakedTextureGetter.apply(gas.getIcon());
		if (sprite == null) {
			return new GasItemBakedModel(ImmutableList.of());
		}

		return new GasItemBakedModel(ItemLayerModel.getQuadsForSprite(0, sprite, format, Optional.empty()));
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}

	private class GasItemBakedModel extends BlankModel {
		ImmutableList<BakedQuad> quads;

		public GasItemBakedModel(ImmutableList<BakedQuad> quads) {
			this.quads = quads;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			if (side != EnumFacing.UP) {
				return ImmutableList.of();
			}
			return quads;
		}

		@Override
		public boolean isGui3d() {
			return false;
		}
	}
}
