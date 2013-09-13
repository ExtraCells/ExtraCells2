package extracells.model.render.tileentity;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.me.tiles.IDirectionalMETile;
import extracells.model.ModelBusFluidExport;
import extracells.model.ModelCable;

public class TileEntityRendererBusFluidExport extends TileEntitySpecialRenderer
{
	private ModelBusFluidExport modelBus = new ModelBusFluidExport();
	private ModelCable modelCable = new ModelCable();

	@Override
	public void renderTileEntityAt(TileEntity tileEntity, double x, double y, double z, float tick)
	{
		modelBus.render(tileEntity, x, y, z);

		int xCoord = tileEntity.xCoord;
		int yCoord = tileEntity.yCoord;
		int zCoord = tileEntity.zCoord;
		World world = tileEntity.worldObj;

		for (ForgeDirection direction : ForgeDirection.values())
		{
			TileEntity offsetTileEntity = world.getBlockTileEntity(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
			if (direction != ForgeDirection.UNKNOWN && tileEntity.getBlockMetadata() != direction.ordinal() && offsetTileEntity instanceof appeng.api.me.tiles.IGridTileEntity)
			{
				if (!(offsetTileEntity instanceof appeng.api.me.tiles.IDirectionalMETile) || (offsetTileEntity instanceof appeng.api.me.tiles.IDirectionalMETile && ((IDirectionalMETile) offsetTileEntity).canConnect(direction.getOpposite())))
				{
					modelCable.render(x, y, z, direction);
				}
			}
		}
	}
}
