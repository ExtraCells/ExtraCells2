package extracells.gui.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.List;

public class WidgetFluidSlot extends Gui
{
	private int id;
	private int posX, posY;
	private Fluid fluid;
	private ResourceLocation slotTexture = new ResourceLocation("extracells", "textures/gui/fluidslot.png");

	public WidgetFluidSlot(int _id, int _posX, int _posY)
	{
		id = _id;
		posX = _posX;
		posY = _posY;
	}

	public int getPosX()
	{
		return posX;
	}

	public int getPosY()
	{
		return posY;
	}

	public void setFluid(Fluid _fluid)
	{
		fluid = _fluid;
	}

	public void drawWidget()
	{
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glColor3f(1.0F, 1.0F, 1.0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(slotTexture);
		drawTexturedModalRect(posX, posY, 0, 0, 18, 18);
		if (fluid != null && fluid.getIcon() != null)
		{
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
			drawTexturedModelRectFromIcon(posX + 1, posY + 1, fluid.getIcon(), 16, 16);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	public void mouseClicked(ItemStack stack)
	{
		fluid = null;
		if (stack == null)
			return;
		Item item = stack.getItem();
		if (item instanceof IFluidContainerItem)
		{
			FluidStack fluidStack = ((IFluidContainerItem) item).getFluid(stack);
			if (fluidStack != null)
				fluid = fluidStack.getFluid();
			return;
		} else if (FluidContainerRegistry.isFilledContainer(stack))
		{
			FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(stack);
			if (fluidStack != null)
				fluid = fluidStack.getFluid();
			return;
		}
		return;
	}

	@SuppressWarnings("rawtypes")
	protected void drawHoveringText(List list, int x, int y, FontRenderer fontrenderer)
	{
		if (!list.isEmpty())
		{
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_DEPTH_TEST);
			int k = 0;

			for (Object string : list)
			{
				String s = (String) string;
				int l = fontrenderer.getStringWidth(s);

				if (l > k)
				{
					k = l;
				}
			}

			int i1 = x + 12;
			int j1 = y - 12;
			int k1 = 8;

			if (list.size() > 1)
			{
				k1 += 2 + (list.size() - 1) * 10;
			}

			this.zLevel = 300.0F;
			int l1 = -267386864;
			this.drawGradientRect(i1 - 3, j1 - 4, i1 + k + 3, j1 - 3, l1, l1);
			this.drawGradientRect(i1 - 3, j1 + k1 + 3, i1 + k + 3, j1 + k1 + 4, l1, l1);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 - 4, j1 - 3, i1 - 3, j1 + k1 + 3, l1, l1);
			this.drawGradientRect(i1 + k + 3, j1 - 3, i1 + k + 4, j1 + k1 + 3, l1, l1);
			int i2 = 1347420415;
			int j2 = (i2 & 16711422) >> 1 | i2 & -16777216;
			this.drawGradientRect(i1 - 3, j1 - 3 + 1, i1 - 3 + 1, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 + k + 2, j1 - 3 + 1, i1 + k + 3, j1 + k1 + 3 - 1, i2, j2);
			this.drawGradientRect(i1 - 3, j1 - 3, i1 + k + 3, j1 - 3 + 1, i2, i2);
			this.drawGradientRect(i1 - 3, j1 + k1 + 2, i1 + k + 3, j1 + k1 + 3, j2, j2);

			for (int k2 = 0; k2 < list.size(); ++k2)
			{
				String s1 = (String) list.get(k2);
				fontrenderer.drawStringWithShadow(s1, i1, j1, -1);

				if (k2 == 0)
				{
					j1 += 2;
				}

				j1 += 10;
			}

			this.zLevel = 0.0F;
			GL11.glEnable(GL11.GL_LIGHTING);
			GL11.glEnable(GL11.GL_DEPTH_TEST);
			RenderHelper.enableStandardItemLighting();
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		}
	}
}
