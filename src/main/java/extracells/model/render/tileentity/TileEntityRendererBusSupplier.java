package extracells.model.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import extracells.model.ModelBusSupplier;

public class TileEntityRendererBusSupplier extends TileEntitySpecialRenderer
{
	private ModelBusSupplier modelBus = new ModelBusSupplier();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
	{
		modelBus.render(tileEntity, x, y, z);
	}
}
