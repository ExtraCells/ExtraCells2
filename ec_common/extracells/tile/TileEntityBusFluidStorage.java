package extracells.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.ICellContainer;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.handler.FluidBusInventoryHandler;

public class TileEntityBusFluidStorage extends TileEntity implements IGridMachine, IDirectionalMETile, ICellContainer
{
	Boolean powerStatus = false;
	IGridInterface grid = null;
	int priority = 1;
	List<ItemStack> filter = new ArrayList<ItemStack>();

	public TileEntityBusFluidStorage()
	{
		powerStatus = false;
	}

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
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

	public ForgeDirection getFacing()
	{
		return ForgeDirection.getOrientation(getBlockMetadata());
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

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public List<IMEInventoryHandler> getCellArray()
	{
		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
		list.add(new FluidBusInventoryHandler(worldObj.getBlockTileEntity(xCoord + getFacing().offsetX, yCoord + getFacing().offsetY, zCoord + getFacing().offsetZ), getFacing(), getPriority(), filter));
		return list;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}
}
