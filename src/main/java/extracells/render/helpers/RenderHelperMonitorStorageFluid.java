package extracells.render.helpers;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;

import org.lwjgl.opengl.GL11;

import extracells.blocks.BlockMonitorStorageFluid;
import extracells.tile.TileEntityMonitorStorageFluid;

public class RenderHelperMonitorStorageFluid extends RenderHelper
{

	public static void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		Tessellator tessellator = Tessellator.instance;
		block.setBlockBoundsForItemRender();
		GL11.glTranslatef(-0.5F, -0.5F, -0.5F);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1.0F, 0.0F);
		renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, metadata));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1.0F);
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, metadata));
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, metadata));
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1.0F, 0.0F, 0.0F);
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);

		tessellator.setBrightness(15 << 20 | 15 << 6);
		tessellator.setColorRGBA_I(0xFFFFFF, 0xFF);
		int[] color =
		{ 0x1B2344, 0x895CA8, 0xDABDEF };
		BlockMonitorStorageFluid terminal = (BlockMonitorStorageFluid) block;
		renderer.renderFaceXNeg(block, 0, 0, 0, terminal.baseLayer);
		for (int i = 0; i < 3; i++)
		{
			tessellator.setColorRGBA_I(color[i], 0xFF);
			renderer.renderFaceXNeg(block, 0, 0, 0, terminal.colorLayers[i]);
		}

		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, metadata));
		tessellator.addTranslation(0.0F, 0.0F, 0.0F);
		tessellator.draw();

		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
	}

	public static boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		Tessellator ts = Tessellator.instance;
		ts.setColorOpaque_I(0xFFFFFF);
		ForgeDirection face = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
		renderer.renderStandardBlock(block, x, y, z);
		BlockMonitorStorageFluid monitor = (BlockMonitorStorageFluid) block;
		int[] color =
		{ 0, 0, 0 };

		switch (((TileEntityMonitorStorageFluid) world.getBlockTileEntity(x, y, z)).getColor())
		{
		case -1:
			color = fluix.clone();
			break;
		case 0:
			color = blue.clone();
			break;
		case 1:
			color = black.clone();
			break;
		case 2:
			color = white.clone();
			break;
		case 3:
			color = brown.clone();
			break;
		case 4:
			color = red.clone();
			break;
		case 5:
			color = yellow.clone();
			break;
		case 6:
			color = green.clone();
			break;
		}

		boolean active = ((TileEntityMonitorStorageFluid) world.getBlockTileEntity(x, y, z)).isMachineActive();
		ts.setBrightness(15 << 2 | 15 << 0);
		if (active)
			ts.setBrightness(15 << 20 | 15 << 4);

		for (int i = 0; i < 3; i++)
		{
			ts.setColorOpaque_I(color[i]);
			drawFace(face, block, x, y, z, monitor.colorLayers[i], renderer);
		}
		return true;
	}
}
