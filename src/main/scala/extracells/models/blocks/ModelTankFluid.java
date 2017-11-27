package extracells.models.blocks;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.lwjgl.util.vector.Vector3f;

import extracells.block.BlockCertusTank;
import extracells.models.BlankModel;
import extracells.models.ModelFactory;

public class ModelTankFluid extends BlankModel {

	public static final Cache<Key, List<BakedQuad>> blockModels = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();
	public static final Cache<Key, List<BakedQuad>> itemModels = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();


	public ModelTankFluid() {
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if (side != null || !(state instanceof IExtendedBlockState)) {
			return ImmutableList.of();
		}
		IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
		FluidStack stack = extendedBlockState.getValue(BlockCertusTank.FLUID);
		int amount = stack.amount / 500;
		if (stack.amount > 0 && amount == 0) {
			amount = 1;
		}
		boolean renderTop = true;
		if(extendedBlockState.getValue(BlockCertusTank.TANK_ABOVE)){
			FluidStack topFluid = extendedBlockState.getValue(BlockCertusTank.FLUID_ABOVE);
			renderTop = topFluid == null || topFluid.getFluid() != stack.getFluid() || topFluid.amount == 0;
		}
		boolean renderBelow = true;
		if(extendedBlockState.getValue(BlockCertusTank.TANK_BELOW)){
			FluidStack bottomFluid = extendedBlockState.getValue(BlockCertusTank.FLUID_BELOW);
			renderBelow = bottomFluid == null || bottomFluid.getFluid() != stack.getFluid() || bottomFluid.amount != 32000;
		}
		Key key = new Key(stack.getFluid(), amount, state.getValue(BlockCertusTank.TANK_ABOVE), state.getValue(BlockCertusTank.TANK_BELOW), renderTop, renderBelow);
		List<BakedQuad> fluidQuads = blockModels.getIfPresent(key);
		if (fluidQuads == null) {
			blockModels.put(key, fluidQuads = createQuads(key));
		}
		return fluidQuads;
	}

	public List<BakedQuad> getQuads(FluidStack stack) {
		int amount = stack.amount / 500;
		if (stack.amount > 0 && amount == 0) {
			amount = 1;
		}
		Key key = new Key(stack.getFluid(), amount, false, false);
		List<BakedQuad> fluidQuads = itemModels.getIfPresent(key);
		if (fluidQuads == null) {
			itemModels.put(key, fluidQuads = createQuads(key));
		}
		return fluidQuads;
	}

	private static ImmutableList<BakedQuad> createQuads(Key key) {
		float maxY = (float) key.amount / 4.0F;
		ResourceLocation resourceLocation = key.fluid.getStill();
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		TextureAtlasSprite sprite = textureMap.getAtlasSprite(resourceLocation.toString());
		if (sprite == null) {
			return ImmutableList.of();
		}
		if (maxY == 16.0F && !key.above) {
			maxY = 15.9F;
		}
		float minY = 0.0F;
		if (!key.below) {
			minY = 0.1F;
		}
		List<EnumFacing> openSides = new ArrayList<EnumFacing>();
		if(!key.renderTop)
			openSides.add(EnumFacing.UP);
		if(!key.renderBelow)
			openSides.add(EnumFacing.DOWN);
		return ModelFactory.createCubeOpen(new Vector3f(1.1F, minY, 1.1F), new Vector3f(14.9F, maxY, 14.9F), sprite, openSides);
	}

	public class Key {
		public Fluid fluid;
		public int amount;
		public boolean above;
		public boolean below;
		public boolean renderTop;
		public boolean renderBelow;

		public Key(Fluid fluid, int amount, boolean above, boolean below) {
			this(fluid, amount, above, below, true, true);
		}

		public Key(Fluid fluid, int amount, boolean above, boolean below, boolean renderTop, boolean renderBelow) {
			this.fluid = fluid;
			this.amount = amount;
			this.above = above;
			this.below = below;
			this.renderTop = renderTop;
			this.renderBelow = renderBelow;
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Key)) {
				return false;
			}
			Key key = (Key) obj;
			return key.fluid == fluid && key.amount == amount && key.above == above && key.below == below && key.renderBelow == renderBelow && key.renderTop == renderTop;
		}

		@Override
		public int hashCode() {
			return fluid.hashCode() + Integer.hashCode(amount) + Boolean.hashCode(above) + Boolean.hashCode(below);
		}
	}
}
