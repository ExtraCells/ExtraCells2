package extracells.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import extracells.render.model.ModelSolderingStation;

public class ItemRendererSolderingStation implements IItemRenderer
{

	private ModelSolderingStation model;

	public ItemRendererSolderingStation()
	{
		model = new ModelSolderingStation();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
	{
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/blocks/texmap_solderingstation.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef(0.5F, 1.5F, 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		model.renderAll(0.0625f);
		GL11.glPopMatrix();
	}

}
