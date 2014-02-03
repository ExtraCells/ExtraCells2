package extracells.inventoryHandler;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.item.ItemStack;

public class HandlerItemStorageFluid implements IMEInventoryHandler<IAEFluidStack>
{
	ItemStack storageStack;

	public HandlerItemStorageFluid(ItemStack _storageStack)
	{
		storageStack = _storageStack;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return null;
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
	public IItemList<IAEFluidStack> getAvailableItems(IItemList out)
	{
		return null;
	}

	@Override
	public StorageChannel getChannel()
	{
		return null;
	}
}
