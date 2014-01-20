package extracells.render;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import extracells.blocks.BlockCertusTank;
import extracells.blocks.BlockMonitorStorageFluid;
import extracells.blocks.BlockTerminalFluid;
import extracells.render.helpers.RenderHelperMonitorStorageFluid;
import extracells.render.helpers.RenderHelperTerminalFluid;
import extracells.render.model.ModelCertusTank;

public class RenderHandler implements ISimpleBlockRenderingHandler
{
	int renderID = 0;
	RenderHelperMonitorStorageFluid monitorRender = new RenderHelperMonitorStorageFluid();
	RenderHelperTerminalFluid terminalRender = new RenderHelperTerminalFluid();
	ModelCertusTank tank = new ModelCertusTank();
	public static int renderPass;

	public RenderHandler(int id)
	{
		RenderHandler.renderPass = 0;
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
		if (block instanceof BlockCertusTank)
		{
			Tessellator tessellator = Tessellator.instance;
			tessellator.setColorOpaque_F(1, 1, 1);
			boolean oldAO = renderer.enableAO;
			renderer.enableAO = false;
			if (RenderHandler.renderPass == 0)
			{
				tank.renderOuterBlock(block, x, y, z, renderer, world);
			} else
			{
				tank.renderInnerBlock(block, x, y, z, renderer, world);
				TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
				tank.renderFluid(tileEntity, x, y, z, renderer);
			}
			renderer.enableAO = oldAO;
			return true;
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
