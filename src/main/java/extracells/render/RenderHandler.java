package extracells.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import extracells.blocks.BlockMonitorStorageFluid;
import extracells.blocks.BlockTerminalFluid;
import extracells.render.helpers.RenderHelperMonitorStorageFluid;
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
			RenderHelperTerminalFluid.renderInventoryBlock(block, metadata, modelID, renderer);
		}
		if (block instanceof BlockMonitorStorageFluid)
		{
			RenderHelperMonitorStorageFluid.renderInventoryBlock(block, metadata, modelID, renderer);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if (block instanceof BlockTerminalFluid)
		{
			return RenderHelperTerminalFluid.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		}
		if (block instanceof BlockMonitorStorageFluid)
		{
			return RenderHelperMonitorStorageFluid.renderWorldBlock(world, x, y, z, block, modelId, renderer);
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
}
