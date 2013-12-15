package extracells.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import extracells.render.model.ModelCable;
import extracells.render.model.ModelLevelEmitterFluid;
import extracells.render.model.ModelCable.Colors;
import extracells.tile.ColorableECTile;

public class TileEntityRendererLevelEmitterFluid extends TileEntitySpecialRenderer
{
	private ModelLevelEmitterFluid modelBus = new ModelLevelEmitterFluid();
	private ModelCable modelCable = new ModelCable();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
	{
		modelBus.render(tileEntity, x, y, z);

		int xCoord = tileEntity.xCoord;
		int yCoord = tileEntity.yCoord;
		int zCoord = tileEntity.zCoord;
		World world = tileEntity.worldObj;

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
