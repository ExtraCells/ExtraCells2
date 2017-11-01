package extracells.models.blocks;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;

import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.Constants;
import extracells.block.BlockCertusTank;
import extracells.models.BlankModel;
import extracells.models.ModelManager;

@SideOnly(Side.CLIENT)
public class ModelCertusTank extends BlankModel {
	public static IBakedModel emptyTank;
	public static IBakedModel emptyTankAbove;
	public static IBakedModel emptyTankBelow;
	public static IBakedModel emptyTankMiddle;
	public static ModelTankFluid fluid = new ModelTankFluid();

	public static void onBakeModels(ModelBakeEvent event) {
		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		ResourceLocation location = new ResourceLocation(Constants.MOD_ID, "certustank");
		emptyTank = registry.getObject(new ModelResourceLocation(location, "above=false,below=false,empty=true"));
		emptyTankAbove = registry.getObject(new ModelResourceLocation(location, "above=true,below=false,empty=true"));
		emptyTankBelow = registry.getObject(new ModelResourceLocation(location, "above=false,below=true,empty=true"));
		emptyTankMiddle = registry.getObject(new ModelResourceLocation(location, "above=true,below=true,empty=true"));
		ModelCertusTank model = new ModelCertusTank();
		registry.putObject(new ModelResourceLocation(location, "above=false,below=false,empty=false"), model);
		registry.putObject(new ModelResourceLocation(location, "above=true,below=false,empty=false"), model);
		registry.putObject(new ModelResourceLocation(location, "above=false,below=true,empty=false"), model);
		registry.putObject(new ModelResourceLocation(location, "above=true,below=true,empty=false"), model);
		registry.putObject(new ModelResourceLocation(location, "inventory"), model);
		ModelTankFluid.blockModels.invalidateAll();
		ModelTankFluid.itemModels.invalidateAll();
	}

	public ModelCertusTank() {
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
		List<BakedQuad> quads = new ArrayList<>();
		if (layer == BlockRenderLayer.TRANSLUCENT) {
			quads.addAll(fluid.getQuads(state, side, rand));
		} else {
			IBakedModel model = emptyTank;
			boolean below = state.getValue(BlockCertusTank.TANK_BELOW);
			boolean above = state.getValue(BlockCertusTank.TANK_ABOVE);
			if (above && !below) {
				model = emptyTankAbove;
			} else if (!above && below) {
				model = emptyTankBelow;
			} else if (above && below) {
				model = emptyTankMiddle;
			}
			quads.addAll(model.getQuads(state, side, rand));
		}
		return quads;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		return emptyTank.getParticleTexture();
	}

	@Override
	protected ItemOverrideList createOverrides() {
		return new TankOverrides();
	}

	private static class ItemModel extends BlankModel {
		FluidStack stack;

		public ItemModel(FluidStack fluidStack) {
			this.stack = fluidStack;
		}

		@Override
		public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
			List<BakedQuad> quads = new ArrayList<>();
			quads.addAll(emptyTank.getQuads(state, side, rand));
			if (stack != null) {
				quads.addAll(fluid.getQuads(stack));
			}
			return quads;
		}
	}

	public static class TankOverrides extends ItemOverrideList {
		public TankOverrides() {
			super(Collections.emptyList());
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
			FluidStack fluidStack = FluidUtil.getFluidContained(stack);
			return new PerspectiveMapWrapper(new ItemModel(fluidStack), ModelManager.getDefaultBlockState());
		}
	}
}
