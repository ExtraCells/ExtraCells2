package extracells.render;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import extracells.block.BlockCertusTank;
import extracells.render.model.ModelCertusTank;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;

public class RenderHandler implements ISimpleBlockRenderingHandler {

	public static int getId() {
		return renderID;
	}

	private static int renderID = 0;
	ModelCertusTank tank = new ModelCertusTank();

	public static int renderPass;

	public RenderHandler(int id) {
		RenderHandler.renderPass = 0;
		renderID = id;
	}

	@Override
	public int getRenderId() {
		return getId();
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID,
			RenderBlocks renderer) {}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z,
			Block block, int modelId, RenderBlocks renderer) {
		if (block instanceof BlockCertusTank) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.setColorOpaque_F(1, 1, 1);
			boolean oldAO = renderer.enableAO;
			renderer.enableAO = false;
			if (RenderHandler.renderPass == 0) {
				this.tank.renderOuterBlock(block, x, y, z, renderer, world);
			} else {
				this.tank.renderInnerBlock(block, x, y, z, renderer, world);
				TileEntity tileEntity = world.getTileEntity(x, y, z);
				this.tank.renderFluid(tileEntity, x, y, z, renderer);
			}
			renderer.enableAO = oldAO;
			return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId) {
		return true;
	}
}
