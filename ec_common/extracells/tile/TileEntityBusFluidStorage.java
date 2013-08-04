package extracells.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.util.IGridInterface;

public class TileEntityBusFluidStorage extends TileEntity implements IGridMachine, IDirectionalMETile
{
	Boolean powerStatus = false;
	IGridInterface grid = null;

	public TileEntityBusFluidStorage()
	{
		powerStatus = false;
	}

	@Override
	public void updateEntity()
	{

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

	public Fluid getFluid()
	{
		if (getTank() != null)
		{
			FluidTankInfo info = getTank().getTankInfo(ForgeDirection.getOrientation(blockMetadata).getOpposite())[0];
			return info.fluid.getFluid();
		} else
		{
			return null;
		}
	}

	public int getCapacity()
	{
		if (getTank() != null)
		{
			FluidTankInfo info = getTank().getTankInfo(ForgeDirection.getOrientation(blockMetadata).getOpposite())[0];
			return info.capacity;
		} else
		{
			return 0;
		}
	}

	public int fillTank(FluidStack resource, Boolean doFill)
	{
		if (getTank() != null)
		{
			return getTank().fill(getFacing().getOpposite(), resource, doFill);
		} else
		{
			return 0;
		}
	}

	public FluidStack drainTank(FluidStack resource, Boolean doDrain)
	{
		return getTank().drain(getFacing().getOpposite(), resource, doDrain);
	}

	public ForgeDirection getFacing()
	{
		return ForgeDirection.getOrientation(blockMetadata);
	}

	public IFluidHandler getTank()
	{
		ForgeDirection facing = ForgeDirection.getOrientation(blockMetadata);
		TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);
		if (facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
		{
			return ((IFluidHandler) worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ));
		} else
		{
			return null;
		}
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
