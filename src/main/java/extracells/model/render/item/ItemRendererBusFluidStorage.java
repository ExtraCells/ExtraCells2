package extracells.model.render.item;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import extracells.model.ModelBusFluidStorage;
import extracells.model.ModelCable;
import extracells.model.ModelCable.Colors;

public class ItemRendererBusFluidStorage implements IItemRenderer
{
	private ModelBusFluidStorage model = new ModelBusFluidStorage();
	private ModelCable cable = new ModelCable();

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
		Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("extracells", "textures/blocks/texmap_storage_bus.png"));
		GL11.glPushMatrix();
		GL11.glTranslatef(-1.0F, 0.5F, 0.5F);
		GL11.glScalef(1.0F, -1F, -1F);
		GL11.glRotatef(180F, 1, 0, 0);
		GL11.glRotatef(-90F, 0, 0, 1);
		model.render(0.0625f);
		cable.renderBase(-0.5, 0.5F, -0.5F, Colors.CLEAR);
		cable.renderExtend(-0.5, 0.5F, -0.5F, ForgeDirection.DOWN, Colors.CLEAR);
		GL11.glPopMatrix();
	}
}
