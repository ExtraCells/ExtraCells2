package extracells.models;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ModelEntry {

	public final ModelResourceLocation modelLocation;
	public final IBakedModel model;

	public ModelEntry(ModelResourceLocation modelLocation, IBakedModel model) {
		this.modelLocation = modelLocation;
		this.model = model;
	}

}
