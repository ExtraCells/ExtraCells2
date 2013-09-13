package extracells.model.render.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import extracells.model.ModelBusFluidImport;

public class ItemRendererBusFluidImport implements IItemRenderer
{
	private ModelBusFluidImport model = new ModelBusFluidImport();

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

	public void renderItem(ItemRenderType type, ItemStack item, Object... data)
	{
		FMLClientHandler.instance().getClient().func_110434_K().func_110577_a(new ResourceLocation("extracells", "textures/blocks/texmap_import_bus.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef(-1.0F, 0.5F, 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glRotatef(180F, 1, 0, 0);
		GL11.glRotatef(-90F, 0, 0, 1);
		model.render(0.0625f);
		GL11.glPopMatrix();
	}
}
