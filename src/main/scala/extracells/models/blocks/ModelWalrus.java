package extracells.models.blocks;

import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
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
import extracells.models.BlankModel;

public class ModelWalrus extends BlankModel {

	static IBakedModel model;

	public static void onBakeModels(ModelBakeEvent event){
		IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
		for(EnumFacing facing : EnumFacing.VALUES) {
			ModelResourceLocation location = new ModelResourceLocation(Constants.MOD_ID +  ":walrus", "facing=" + facing.getName());
			registry.putObject(location, new ModelWalrus());
		}
		model = null;
	}

	@Override
	public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
		if(model == null){
			ResourceLocation modelLocation = new ResourceLocation(Constants.MOD_ID,"block/walrus.obj");
			IModel model = ModelLoaderRegistry.getModelOrMissing(modelLocation);
			model = ModelProcessingHelper.retexture(model, ImmutableMap.of("#builtin/white", Constants.MOD_ID + ":blocks/certustank"));
			ModelWalrus.model = model.bake(ModelRotation.X0_Y0, DefaultVertexFormats.BLOCK, (k)-> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(k.toString()));
		}
		return ModelWalrus.model.getQuads(state, side, rand);
	}
}
