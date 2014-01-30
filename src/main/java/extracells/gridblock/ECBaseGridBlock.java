package extracells.gridblock;

import appeng.api.networking.*;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.EnumSet;

public abstract class ECBaseGridBlock implements IGridBlock
{
	World world;
	int x, y, z;
	AEColor color;
	IGrid grid;
	int usedChannels;
	IGridHost host;

	protected ECBaseGridBlock(IGridHost _host)
	{
		host = _host;
	}

	@Override
	public abstract double getIdlePowerUsage();

	@Override
	public EnumSet<GridFlags> getFlags()
	{
		return null;
	}

	@Override
	public final boolean isWorldAccessable()
	{
		return false;
	}

	@Override
	public final DimensionalCoord getLocation()
	{
		return new DimensionalCoord(world, x, y, z);
	}

	@Override
	public final AEColor getGridColor()
	{
		return color;
	}

	@Override
	public void onGridNotification(GridNotification notification)
	{
	}

	@Override
	public final void setNetworkStatus(IGrid _grid, int _usedChannels)
	{
		grid = _grid;
		usedChannels = _usedChannels;
	}

	@Override
	public final EnumSet<ForgeDirection> getConnectableSides()
	{
		return null;
	}

	@Override
	public IGridHost getMachine()
	{
		return host;
	}

	@Override
	public void gridChanged()
	{
	}

	@Override
	public ItemStack getMachineRepresentation()
	{
		return null;//TODO
	}
}
