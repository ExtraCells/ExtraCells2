package extracells.render.tileentity;

import extracells.render.model.ModelCertusTank;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;

public class TileEntityRendererCertusTank extends TileEntitySpecialRenderer
{
	private ModelCertusTank modelTank = new ModelCertusTank();

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float partialTickTime)
	{
		modelTank.render(tileentity, x, y, z);
	}
}
