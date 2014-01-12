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
	RenderHelperMonitorStorageFluid monitorRender = new RenderHelperMonitorStorageFluid();
	RenderHelperTerminalFluid terminalRender = new RenderHelperTerminalFluid();

	public RenderHandler(int id)
	{
		renderID = id;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (block instanceof BlockTerminalFluid)
		{
			terminalRender.renderInventoryBlock(block, metadata, modelID, renderer);
		}
		if (block instanceof BlockMonitorStorageFluid)
		{
			monitorRender.renderInventoryBlock(block, metadata, modelID, renderer);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if (block instanceof BlockTerminalFluid)
		{
			return terminalRender.renderWorldBlock(world, x, y, z, block, modelId, renderer);
		}
		if (block instanceof BlockMonitorStorageFluid)
		{
			return monitorRender.renderWorldBlock(world, x, y, z, block, modelId, renderer);
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
