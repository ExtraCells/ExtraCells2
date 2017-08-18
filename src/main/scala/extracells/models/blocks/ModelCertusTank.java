package extracells.models.blocks;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import extracells.models.BlankModel;

public class ModelCertusTank extends BlankModel {
	public ModelCertusTank() {
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		return super.getQuads(state, side, rand);
	}

	@Override
	protected ItemOverrideList createOverrides() {
		return super.createOverrides();
	}

	public static class TankOverrides extends ItemOverrideList{
		public TankOverrides(List<ItemOverride> overridesIn) {
			super(overridesIn);
		}

		@Override
		public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
			return super.handleItemState(originalModel, stack, world, entity);
		}
	}
}
