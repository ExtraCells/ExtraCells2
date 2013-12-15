package extracells.render.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public class ModelWalrus extends ModelBase
{
	public IModelCustom model;

	public ModelWalrus()
	{
		model = AdvancedModelLoader.loadModel("/assets/extracells/models/walrus.obj");
	}

	public void render()
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/blocks/walrus.png"));
		model.renderAll();
	}
}
