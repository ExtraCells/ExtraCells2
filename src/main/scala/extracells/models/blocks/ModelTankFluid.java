package extracells.models.blocks;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.BlockPartFace;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ModelRotation;
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

public class ModelTankFluid extends BlankModel {

	private static final FaceBakery bakery = new FaceBakery();
	public static final Cache<Key, List<BakedQuad>> models = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

	public ModelTankFluid() {
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if(side == null || !(state instanceof IExtendedBlockState)){
			return ImmutableList.of();
		}
		IExtendedBlockState extendedBlockState = (IExtendedBlockState) state;
		FluidStack stack = extendedBlockState.getValue(BlockCertusTank.FLUID);
		int amount = stack.amount / 500;
		if(stack.amount > 0 && amount == 0){
			amount = 1;
		}
		Key key = new Key(stack.getFluid(), amount, state.getValue(BlockCertusTank.TANK_ABOVE), state.getValue(BlockCertusTank.TANK_BELOW));
		List<BakedQuad> fluidQuads = models.getIfPresent(key);
		if(fluidQuads == null){
			models.put(key, fluidQuads = createQuads(key));
		}
		return Collections.singletonList(fluidQuads.get(side.getIndex()));
	}

	private static ImmutableList<BakedQuad> createQuads(Key key){
		float maxY = (float )key.amount / 4.0F;
		ResourceLocation resourceLocation = key.fluid.getStill();
		TextureMap textureMap = Minecraft.getMinecraft().getTextureMapBlocks();
		TextureAtlasSprite sprite = textureMap.getAtlasSprite(resourceLocation.toString());
		if(sprite == null){
			return ImmutableList.of();
		}
		if(maxY == 16.0F && !key.above){
			maxY = 15.9F;
		}
		float minY = 0.0F;
		if(!key.below){
			maxY = 0.1F;
		}
		return bakeFluidModel(new Vector3f(1.1F, minY, 1.1F), new Vector3f(14.9F, maxY, 14.9F), sprite);
	}

	private static ImmutableList<BakedQuad> bakeFluidModel(Vector3f from, Vector3f to, TextureAtlasSprite sprite){
		ImmutableList.Builder builder = new ImmutableList.Builder();
		for(EnumFacing facing : EnumFacing.VALUES) {
			float[] uvs = getFaceUvs(facing, to, from);
			BlockFaceUV uv = new BlockFaceUV(uvs, 0);
			BlockPartFace bpf = new BlockPartFace(facing, 0, "", uv);
			builder.add(bakery.makeBakedQuad(from, to, bpf, sprite, facing, ModelRotation.X0_Y0, null, true, true));
		}
		return builder.build();
	}

	protected static float[] getFaceUvs(EnumFacing face, Vector3f to, Vector3f from) {
		float minU;
		float minV;
		float maxU;
		float maxV;
		switch (face) {
			case SOUTH: {
				minU = from.x;
				minV = from.y;
				maxU = to.x;
				maxV = to.y;
				break;
			}
			case NORTH: {
				minU = from.x;
				minV = from.y;
				maxU = to.x;
				maxV = to.y;
				break;
			}
			case WEST: {
				minU = from.z;
				minV = from.y;
				maxU = to.z;
				maxV = to.y;
				break;
			}
			case EAST: {
				minU = from.z;
				minV = from.y;
				maxU = to.z;
				maxV = to.y;
				break;
			}
			case UP: {
				minU = from.x;
				minV = from.z;
				maxU = to.x;
				maxV = to.z;
				break;
			}
			case DOWN: {
				minU = from.x;
				minV = from.z;
				maxU = to.x;
				maxV = to.z;
				break;
			}
			default: {
				minU = 0;
				minV = 0;
				maxU = 16;
				maxV = 16;
				break;
			}
		}
		if (minU < 0 || maxU > 16) {
			minU = 0;
			maxU = 16;
		}
		if (minV < 0 || maxV > 16) {
			minV = 0;
			maxV = 16;
		}
		minU = 16 - minU;
		minV = 16 - minV;
		maxU = 16 - maxU;
		maxV = 16 - maxV;
		return new float[]{minU, minV, maxU, maxV};
	}

	public class Key{
		public Fluid fluid;
		public int amount;
		public boolean above;
		public boolean below;

		public Key(Fluid fluid, int amount, boolean above, boolean below) {
			this.fluid = fluid;
			this.amount = amount;
			this.above = above;
			this.below = below;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof Key)){
				return false;
			}
			Key key = (Key) obj;
			return key.fluid == fluid && key.amount == amount && key.above == above && key.below == below;
		}

		@Override
		public int hashCode() {
			return fluid.hashCode() + Integer.hashCode(amount) + Boolean.hashCode(above) + Boolean.hashCode(below);
		}
	}
}
