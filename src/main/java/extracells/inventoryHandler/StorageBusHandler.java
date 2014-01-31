package extracells.inventoryHandler;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.part.FluidStorage;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.List;

public class StorageBusHandler implements IMEInventoryHandler<IAEFluidStack>
{
	private FluidStorage node;
	private IFluidHandler tank;
	private AccessRestriction access;
	private List<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	private ForgeDirection orientation;

	public StorageBusHandler(FluidStorage _node, IFluidHandler _tank, ForgeDirection _orientation)
	{
		node = _node;
		tank = _tank;
		orientation = _orientation;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return access;
	}

	@Override
	public boolean isPrioritized(IAEFluidStack input)
	{
		if (input == null)
			return false;
		for (Fluid fluid : prioritizedFluids)
			if (fluid == input.getFluid())
				return true;
		return false;
	}

	@Override
	public boolean canAccept(IAEFluidStack input)
	{
		if (tank == null)
			return false;
		FluidTankInfo[] infoArray = tank.getTankInfo(orientation.getOpposite());
		if (infoArray != null && infoArray.length > 0)
		{
			FluidTankInfo info = infoArray[0];
			if (info.fluid == null || info.fluid.amount == 0 || info.fluid.fluidID == input.getFluidStack().fluidID)
				return prioritizedFluids.isEmpty() ? true : isPrioritized(input) ? true : false;
		}
		return false;
	}

	@Override
	public int getPriority()
	{
		return node.getPriority();
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

	@Override
	public IAEFluidStack injectItems(IAEFluidStack input, Actionable mode, BaseActionSource src)
	{
		if (tank == null || input == null)
			return input;
		FluidStack toFill = input.getFluidStack();
		int filled = tank.fill(orientation.getOpposite(), toFill.copy(), mode == Actionable.MODULATE);
		if (filled == toFill.amount)
			return null;
		return AEApi.instance().storage().createFluidStack(new FluidStack(toFill.fluidID, toFill.amount - filled));
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src)
	{
		if (tank == null || request == null)
			return null;
		FluidStack toDrain = request.getFluidStack();
		FluidStack drained = tank.drain(orientation.getOpposite(), toDrain.copy(), mode == Actionable.MODULATE);
		if (drained != null)
		{
			int amount = drained.amount;
			if (amount == toDrain.amount)
				return request;
			return AEApi.instance().storage().createFluidStack(new FluidStack(toDrain.fluidID, amount));
		} else
		{
			return null;
		}

	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		if (tank == null)
		{
			FluidTankInfo[] infoArray = tank.getTankInfo(orientation.getOpposite());
			if (infoArray != null && infoArray.length > 0)
				out.add(AEApi.instance().storage().createFluidStack(infoArray[0].fluid));
		}
		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.FLUIDS;
	}
}
