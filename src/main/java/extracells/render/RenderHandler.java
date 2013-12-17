package extracells.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import extracells.blocks.BlockTerminalFluid;
import extracells.render.helpers.RenderHelperTerminalFluid;

public class RenderHandler implements ISimpleBlockRenderingHandler
{
	int renderID = 0;

	public RenderHandler(int id)
	{
		renderID = id;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (block instanceof BlockTerminalFluid)
		{
			new RenderHelperTerminalFluid().renderInventoryBlock(this, block, metadata, modelID, renderer);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if (block instanceof BlockTerminalFluid)
		{
			return new RenderHelperTerminalFluid().renderWorldBlock(this, world, x, y, z, block, modelId, renderer);
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return renderID;
	}

	public void drawFace(ForgeDirection side, Block block, double x, double y, double z, Icon icon, RenderBlocks renderer)
	{
		switch (side)
		{
		case UP:
			renderer.renderFaceYPos(block, x, y, z, icon);
			break;
		case DOWN:
			renderer.renderFaceYNeg(block, x, y, z, icon);
			break;
		case NORTH:
			renderer.renderFaceZNeg(block, x, y, z, icon);
			break;
		case EAST:
			renderer.renderFaceXPos(block, x, y, z, icon);
			break;
		case SOUTH:
			renderer.renderFaceZPos(block, x, y, z, icon);
			break;
		case WEST:
			renderer.renderFaceXNeg(block, x, y, z, icon);
			break;
		case UNKNOWN:
			break;
		}
	}

}
