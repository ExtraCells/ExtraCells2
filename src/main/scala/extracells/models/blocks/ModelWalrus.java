package extracells.models.blocks;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;

import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelProcessingHelper;

import extracells.Constants;
import extracells.block.BlockWalrus;
import extracells.models.BlankModel;

public class ModelWalrus extends BlankModel {

	static EnumMap<EnumFacing, IBakedModel> bakedModels = new EnumMap(EnumFacing.class);
	static EnumMap<EnumFacing, IModel> models = new EnumMap(EnumFacing.class);

	public static void onBakeModels(ModelBakeEvent event){
		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		for(EnumFacing facing : EnumFacing.VALUES) {
			ModelResourceLocation location = new ModelResourceLocation(Constants.MOD_ID +  ":walrus", "facing=" + facing.getName());
			models.put(facing, ModelLoaderRegistry.getModelOrMissing(location));
			registry.putObject(location, new ModelWalrus());
		}
		bakedModels.clear();
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		EnumFacing facing = state.getValue(BlockWalrus.FACING);
		IBakedModel bakedModel = bakedModels.get(facing);
		if (bakedModel == null) {
			ResourceLocation modelLocation = new ResourceLocation(Constants.MOD_ID,"block/walrus.obj");
			IModel model = ModelLoaderRegistry.getModelOrMissing(modelLocation);
			model = ModelProcessingHelper.retexture(model, ImmutableMap.of("#builtin/white", Constants.MOD_ID + ":blocks/walrus"));
			//model = ModelProcessingHelper.customData(model, ImmutableMap.of("flip-v", "true"));
			ModelRotation rotation;
			switch (facing) {
				case EAST:
					rotation = ModelRotation.X0_Y90;
					break;
				case SOUTH:
					rotation = ModelRotation.X0_Y180;
					break;
				case WEST:
					rotation = ModelRotation.X0_Y270;
					break;
				default:
					rotation = ModelRotation.X0_Y0;
					break;
			}
			bakedModel = model.bake(rotation, DefaultVertexFormats.ITEM, (k) ->
				Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(k.toString())
			);
			bakedModels.put(facing, bakedModel);
		}
		return bakedModel.getQuads(state, side, rand);
	}
}
