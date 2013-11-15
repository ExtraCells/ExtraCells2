package extracells.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import extracells.model.ModelWalrus;

public class TileEntityRedererWalrus extends TileEntitySpecialRenderer
{
	ModelWalrus model = new ModelWalrus();

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime)
	{
		GL11.glPushMatrix();
		GL11.glTranslated(x + 0.5, y, z + 0.5);
		int orientation = tileentity.worldObj.getBlockMetadata(tileentity.xCoord, tileentity.yCoord, tileentity.zCoord);
		if (orientation == 4)
		{
			GL11.glRotatef(90, 0, 1, 0);
		} else if (orientation == 5)
		{
			GL11.glRotatef(-90, 0, 1, 0);
		} else if (orientation == 3)
		{
			GL11.glRotatef(180, 0, 1, 0);
		}
		model.render();
		GL11.glPopMatrix();
	}
}
