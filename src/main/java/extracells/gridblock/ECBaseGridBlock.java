package extracells.gridblock;

import appeng.api.networking.*;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import extracells.part.ECBasePart;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.EnumSet;

public class ECBaseGridBlock implements IGridBlock
{
	protected World world;
	protected int x, y, z;
	protected AEColor color;
	protected IGrid grid;
	protected int usedChannels;
	protected ECBasePart host;

	public ECBaseGridBlock(ECBasePart _host)
	{
		host = _host;
	}

	@Override
	public double getIdlePowerUsage()
	{
		return host.getPowerUsage();
	}

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
		return null;// TODO
	}
}
