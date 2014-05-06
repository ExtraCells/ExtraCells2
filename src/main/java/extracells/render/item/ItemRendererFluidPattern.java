package extracells.render.item;

import extracells.util.GuiUtil;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.IItemRenderer;
import org.lwjgl.opengl.GL11;

public class ItemRendererFluidPattern implements IItemRenderer
{

	@Override
	public boolean handleRenderType(ItemStack itemStack, ItemRenderType type)
	{
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack itemStack, ItemRendererHelper helper)
	{
		return type == ItemRenderType.ENTITY;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack itemStack, Object... data)
	{
		GL11.glEnable(GL11.GL_BLEND);

		if (type == ItemRenderType.ENTITY)
		{
			if (RenderItem.renderInFrame)
			{
				GL11.glScaled(1 / 16, 1 / 16, 1 / 16);
				GL11.glRotatef(-90, 0, 1, 0);
				GL11.glTranslated(-.5, -.42, 0);
			} else
			{
				GL11.glTranslated(-.5, -.42, 0);
			}
		}
		GL11.glColor3f(1, 1, 1);
		GuiUtil.drawIcon(Blocks.stone.getIcon(0, 0), 0, 0, 0, 1, 1);

	}
}
