package extracells.tile;

import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.TileRef;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.exceptions.AppEngTileMissingException;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.util.IGridInterface;

public class TileEntityBusFluidImport extends TileEntity implements IGridMachine, IDirectionalMETile
{
	Boolean powerStatus;
	IGridInterface grid;

	public TileEntityBusFluidImport()
	{
		powerStatus = false;
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			ForgeDirection facing = ForgeDirection.getOrientation(blockMetadata);
			TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);
			if (grid != null && facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
			{
				List<TileRef<IGridMachine>> tilelist = grid.getMachines();
				IFluidHandler source = (IFluidHandler) facingTileEntity;

				for (TileRef<IGridMachine> entry : tilelist)
				{
					try
					{
						if (grid != null)
						{
							if (entry.getTile() instanceof TileEntityBusFluidStorage)
							{

								if (source.getTankInfo(facing.getOpposite())[0].fluid != null)
								{
									FluidStack todrain = new FluidStack(source.getTankInfo(facing.getOpposite())[0].fluid, 50);
									TileEntityBusFluidStorage entity = ((TileEntityBusFluidStorage) worldObj.getBlockTileEntity(entry.getTile().getLocation().x, entry.getTile().getLocation().y, entry.getTile().getLocation().z));
									if (source.drain(entity.getFacing().getOpposite(), todrain, false).amount == 50)
									{
										if (entity.fillTank(todrain, false) == 50)
										{
											((IFluidHandler) worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ)).drain(facing.getOpposite(), todrain, true);
											((TileEntityBusFluidStorage) worldObj.getBlockTileEntity(entry.getTile().getLocation().x, entry.getTile().getLocation().y, entry.getTile().getLocation().z)).fillTank(todrain, true);
											break;
										}
									}
								}
							}
						}
					} catch (AppEngTileMissingException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
	}

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
	}

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower)
	{
		powerStatus = hasPower;
	}

	@Override
	public boolean isPowered()
	{
		return powerStatus;
	}

	@Override
	public IGridInterface getGrid()
	{
		return grid;
	}

	@Override
	public void setGrid(IGridInterface gi)
	{
		grid = gi;
	}

	@Override
	public World getWorld()
	{
		return worldObj;
	}

	@Override
	public boolean canConnect(ForgeDirection dir)
	{
		return dir.ordinal() != this.blockMetadata;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 0;
	}
}
