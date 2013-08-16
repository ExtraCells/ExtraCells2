package extracells.tile;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;

public class TileEntityBusFluidImport extends TileEntity implements IGridMachine, IDirectionalMETile
{
	Boolean powerStatus = false;
	IGridInterface grid;

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote && isPowered())
		{
			ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata());
			TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);

			if (grid != null && facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
			{
				IFluidHandler tank = (IFluidHandler) facingTileEntity;
				FluidStack fluidStack = tank.getTankInfo(facing)[0].fluid;

				if (fluidStack != null)
				{
					Fluid fluid = tank.getTankInfo(facing)[0].fluid.getFluid();

					IAEItemStack toImport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluid.getID()));
					toImport.setStackSize(tank.drain(facing, new FluidStack(fluid, 20), false).amount);
					IAEItemStack notImported = grid.getCellArray().addItems(toImport.copy());
					IAEItemStack imported = toImport.copy();

					if (notImported != null)
						imported.setStackSize(toImport.getStackSize() - notImported.getStackSize());

					if (imported != null && grid.useMEEnergy(12.0F, "Import Fluid"))
						tank.drain(facing, new FluidStack(fluid, (int) imported.getStackSize()), true);
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
