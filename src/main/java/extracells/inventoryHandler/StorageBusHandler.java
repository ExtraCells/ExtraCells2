package extracells.inventoryHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import net.minecraftforge.fluids.IFluidHandler;

public class StorageBusHandler implements IMEInventoryHandler<IAEFluidStack>
{
	private IGridNode node;
	private IFluidHandler tank;
	private AccessRestriction access;

	public StorageBusHandler(IGridNode _node, IFluidHandler _tank)
	{
		node = _node;
		tank = _tank;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return access;
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input)
	{
		return false;
	}

	@Override
	public boolean canAccept(IAEFluidStack input)
	{
		return false;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable type, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src)
	{
		return null;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		return null;
	}

	@Override
	public StorageChannel getChannel()
	{
		return null;
	}
}
