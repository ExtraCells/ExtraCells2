package extracells.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import extracells.render.model.ModelBusFluidImport;
import extracells.render.model.ModelCable;
import extracells.render.model.ModelCable.Colors;
import extracells.tile.ColorableECTile;

public class TileEntityRendererBusFluidImport extends TileEntitySpecialRenderer
{
	private ModelBusFluidImport modelBus = new ModelBusFluidImport();
	private ModelCable modelCable = new ModelCable();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
	{
		modelBus.render(tileEntity, x, y, z);

		if (((ColorableECTile) tileEntity).getVisualConnections() != null)
		{
			for (ForgeDirection direction : ((ColorableECTile) tileEntity).getVisualConnections())
			{
				modelCable.renderExtend(x, y, z, direction, Colors.getColorByID(((ColorableECTile) tileEntity).getColor()));
			}
		}
		modelCable.renderBase(x, y, z, Colors.getColorByID(((ColorableECTile) tileEntity).getColor()));
	}
}
