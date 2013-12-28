package extracells.render.tileentity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import org.lwjgl.opengl.GL11;

import extracells.render.model.ModelWalrus;
import extracells.tile.TileEntityMonitorStorageFluid;

public class TileEntityRendererMonitorStorageFluid extends TileEntitySpecialRenderer
{
	ModelWalrus model = new ModelWalrus();

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime)
	{
		if (tileentity instanceof TileEntityMonitorStorageFluid)
		{
			Fluid fluid = ((TileEntityMonitorStorageFluid) tileentity).getFluid();
			if (fluid == null || fluid.getIcon() == null)
				return;
			Icon fluidIcon = fluid.getFlowingIcon();

			GL11.glPushMatrix();
			GL11.glPushAttrib(1048575);

			FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
			ForgeDirection d = ForgeDirection.getOrientation(tileentity.blockMetadata);
			GL11.glTranslated(x + 0.5D, y + 0.5D, z + 0.5D);
			GL11.glTranslated(d.offsetX * 0.76D, d.offsetY * 0.76D, d.offsetZ * 0.76D);
			if (d == ForgeDirection.UP)
			{
				GL11.glScalef(1.0F, -1.0F, 1.0F);
				GL11.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(90.0F, 0.0F, 0.0F, 1.0F);
			}

			if (d == ForgeDirection.DOWN)
			{
				GL11.glScalef(1.0F, -1.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(-90.0F, 0.0F, 0.0F, 1.0F);
			}

			if (d == ForgeDirection.EAST)
			{
				GL11.glScalef(-1.0F, -1.0F, -1.0F);
				GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
			}

			if (d == ForgeDirection.WEST)
			{
				GL11.glScalef(-1.0F, -1.0F, -1.0F);
				GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
			}

			if (d == ForgeDirection.NORTH)
			{
				GL11.glScalef(-1.0F, -1.0F, -1.0F);
			}

			if (d == ForgeDirection.SOUTH)
			{
				GL11.glScalef(-1.0F, -1.0F, -1.0F);
				GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			}

			GL11.glTranslatef(0.0F, 0.14F, -0.24F);
			GL11.glScalef(0.01612903F, 0.01612903F, 0.01612903F);

			long qty = ((TileEntityMonitorStorageFluid) tileentity).getAmount();
			if (qty > 999999999999L)
			{
				qty = 999999999999L;
			}
			String msg = Long.toString(qty);
			if (qty > 1000000000L)
				msg = Long.toString(qty / 1000000000L) + "B";
			else if (qty > 1000000L)
				msg = Long.toString(qty / 1000000L) + "M";
			else if (qty > 9999L)
			{
				msg = Long.toString(qty / 1000L) + "K";
			}
			int width = fr.getStringWidth(msg);
			GL11.glTranslatef(-0.5F * width, 0.0F, -1.0F);
			fr.drawString(msg, 0, 0, 0x00FFFF);
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);

			GL11.glTranslated(-5, -16.5, 0.1F);
			Tessellator cake = Tessellator.instance;
			cake.startDrawingQuads();
			cake.setBrightness(255);
			cake.setColorRGBA_F(1.0f, 1.0f, 1.0f, 1.0f);
			cake.addVertexWithUV(0, 16, 0, fluidIcon.getMinU(), fluidIcon.getMaxV());
			cake.addVertexWithUV(16, 16, 0, fluidIcon.getMaxU(), fluidIcon.getMaxV());
			cake.addVertexWithUV(16, 0, 0, fluidIcon.getMaxU(), fluidIcon.getMinV());
			cake.addVertexWithUV(0, 0, 0, fluidIcon.getMinU(), fluidIcon.getMinV());
			cake.draw();

			GL11.glPopMatrix();
			GL11.glPopAttrib();
		}
	}
}
