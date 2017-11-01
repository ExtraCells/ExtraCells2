package extracells.models.blocks;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import extracells.Constants;
import extracells.models.BlankModel;
import extracells.registries.ItemEnum;

public class FluidItemModel implements IModel {

	public static class ModelLoader implements ICustomModelLoader {
		@Override
		public boolean accepts(ResourceLocation modelLocation) {
			return modelLocation.getResourceDomain().equals(Constants.MOD_ID)
				&& modelLocation.getResourcePath().contains("models/item/fluid/");
		}

		@Override
		public IModel loadModel(ResourceLocation modelLocation) throws Exception {
			String fluidName = modelLocation.getResourcePath().replace("models/item/fluid/", "");
			Fluid fluid = FluidRegistry.getFluid(fluidName);
			if (fluid == null) {
				fluid = FluidRegistry.WATER;
			}
			return new FluidItemModel(fluid);
		}

		@Override
		public void onResourceManagerReload(IResourceManager resourceManager) {
			for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
				ModelBakery.registerItemVariants(ItemEnum.FLUIDITEM.getItem(), new ResourceLocation(Constants.MOD_ID + ":fluid/" + fluid.getName()));
			}
		}
	}

	public Fluid fluid;

	public FluidItemModel(Fluid fluid) {
		this.fluid = fluid;
	}

	@Override
	public Collection<ResourceLocation> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public Collection<ResourceLocation> getTextures() {
		return Collections.singletonList(fluid.getStill());
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
		TextureAtlasSprite sprite = bakedTextureGetter.apply(fluid.getStill());
		if (sprite == null) {
			return new FluidItemBakedModel(ImmutableList.of());
		}

		return new FluidItemBakedModel(ItemLayerModel.getQuadsForSprite(0, sprite, format, Optional.empty()));
	}

	@Override
	public IModelState getDefaultState() {
		return TRSRTransformation.identity();
	}

	private class FluidItemBakedModel extends BlankModel {
		ImmutableList<BakedQuad> quads;

		public FluidItemBakedModel(ImmutableList<BakedQuad> quads) {
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
