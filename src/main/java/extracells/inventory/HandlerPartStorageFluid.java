package extracells.inventory;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import extracells.part.PartFluidStorage;
import extracells.util.FluidUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HandlerPartStorageFluid implements IMEInventoryHandler<IAEFluidStack>
{
	private PartFluidStorage node;
	private IFluidHandler tank;
	private AccessRestriction access;
	private List<Fluid> prioritizedFluids = new ArrayList<Fluid>();
	private boolean inverted;

	public HandlerPartStorageFluid(PartFluidStorage _node)
	{
		node = _node;
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
		if (tank == null || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS)
			return false;
		FluidTankInfo[] infoArray = tank.getTankInfo(node.getSide().getOpposite());
		if (infoArray != null && infoArray.length > 0)
		{
			FluidTankInfo info = infoArray[0];
			if (info.fluid == null || info.fluid.amount == 0 || info.fluid.fluidID == input.getFluidStack().fluidID)
				if (inverted)
					return !prioritizedFluids.isEmpty() || !isPrioritized(input);
				else
					return prioritizedFluids.isEmpty() || isPrioritized(input);
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
		if (tank == null || input == null || !canAccept(input))
			return input;
		FluidStack toFill = input.getFluidStack();
		int filled = tank.fill(node.getSide().getOpposite(), toFill.copy(), mode == Actionable.MODULATE);
		if (filled == toFill.amount)
			return null;
		System.out.println(filled);
		return FluidUtil.createAEFluidStack(toFill.fluidID, toFill.amount - filled);
	}

	@Override
	public IAEFluidStack extractItems(IAEFluidStack request, Actionable mode, BaseActionSource src)
	{
		if (tank == null || request == null || access == AccessRestriction.WRITE || access == AccessRestriction.NO_ACCESS)
			return null;
		FluidStack toDrain = request.getFluidStack();
		FluidStack drained = tank.drain(node.getSide().getOpposite(), toDrain.copy(), mode == Actionable.MODULATE);

		if (drained != null)
		{
			int amount = drained.amount;
			if (amount == toDrain.amount)
				return request;
			return FluidUtil.createAEFluidStack(toDrain.fluidID, amount);
		} else
		{
			return null;
		}

	}

	@Override
	public IItemList<IAEFluidStack> getAvailableItems(IItemList<IAEFluidStack> out)
	{
		if (tank != null)
		{
			FluidTankInfo[] infoArray = tank.getTankInfo(node.getSide().getOpposite());
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

	public void onNeighborChange()
	{
		tank = null;
		ForgeDirection orientation = node.getSide();
		TileEntity hostTile = node.getHostTile();
		if (hostTile == null)
			return;
		if (hostTile.getWorldObj() == null)
			return;
		TileEntity tileEntity = hostTile.getWorldObj().getTileEntity(hostTile.xCoord + orientation.offsetX, hostTile.yCoord + orientation.offsetY, hostTile.zCoord + orientation.offsetZ);
		tank = null;
		if (tileEntity instanceof IFluidHandler)
			tank = (IFluidHandler) tileEntity;
	}

	public void setInverted(boolean _inverted)
	{
		inverted = _inverted;
	}

	public void setPrioritizedFluids(Fluid[] _fluids)
	{
		prioritizedFluids = Arrays.asList(_fluids);
	}
}
