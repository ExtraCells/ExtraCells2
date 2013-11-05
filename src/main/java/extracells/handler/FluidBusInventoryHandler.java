package extracells.handler;

import java.util.ArrayList;
import java.util.List;

import extracells.ItemEnum;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.config.FuzzyMode;
import appeng.api.config.ItemFlow;
import appeng.api.config.ListMode;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import net.minecraftforge.fluids.IFluidHandler;

public class FluidBusInventoryHandler implements IMEInventoryHandler
{
	public TileEntity tank;
	ForgeDirection facing;
	public int priority;
	List<ItemStack> filter;
	public TileEntity updateTarget;
	public IGridInterface grid;
	public IMEInventoryHandler parent;

	public FluidBusInventoryHandler(TileEntity tank, ForgeDirection facing, int priority, List<ItemStack> filter)
	{
		this.tank = tank;
		this.facing = facing;
		this.priority = priority;
		this.filter = filter;
	}

	@Override
	public long storedItemTypes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return getTankInfo(tank)[0].fluid != null ? 1 : 0;
		}
		return 0;
	}

	@Override
	public long storedItemCount()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return getTankInfo(tank)[0].fluid.amount;
		}
		return 0;
	}

	@Override
	public long remainingItemCount()
	{
		if (tank != null && tank instanceof IFluidHandler && getTankInfo(tank)[0].fluid != null)
		{
			return getTankInfo(tank)[0].capacity - getTankInfo(tank)[0].fluid.amount;
		}
		return 0;
	}

	@Override
	public long remainingItemTypes()
	{
		if (tank != null && tank instanceof IFluidHandler && getTankInfo(tank)[0].fluid == null)
		{
			return 1;
		}
		return 0;
	}

	@Override
	public boolean containsItemType(IAEItemStack aeitemstack)
	{
		if (tank != null && tank instanceof IFluidHandler && getTankInfo(tank)[0] != null && getTankInfo(tank)[0].fluid != null)
		{
			return aeitemstack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && aeitemstack.getItemDamage() == ((IFluidHandler) tank).getTankInfo(facing.getOpposite())[0].fluid.fluidID;
		}
		return false;
	}

	@Override
	public long getTotalItemTypes()
	{
		return 1;
	}

	@Override
	public long countOfItemType(IAEItemStack aeitemstack)
	{
		if (tank != null && tank instanceof IFluidHandler && getTankInfo(tank)[0] != null && getTankInfo(tank)[0].fluid != null)
		{
			return aeitemstack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() ? aeitemstack.getItemDamage() == getTankInfo(tank)[0].fluid.fluidID ? getTankInfo(tank)[0].fluid.amount : 0 : 0;
		}
		return 0;
	}

	@Override
	public IAEItemStack addItems(IAEItemStack input)
	{
		IAEItemStack addedStack = input.copy();

		if (input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (tank instanceof IFluidHandler)
			{

				if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid == null || FluidRegistry.getFluid(input.getItemDamage()) == ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid())
				{

					int filled = 0;

					for (long i = 0; i < input.getStackSize(); i++)
					{
						filled += ((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), 1), true);
					}

					addedStack.setStackSize(input.getStackSize() - filled);

					tank.onInventoryChanged();

					if (addedStack != null && addedStack.getStackSize() == 0)
						addedStack = null;

					return addedStack;
				}
			}
		}
		return addedStack;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request)
	{
		IAEItemStack removedStack = request.copy();

		if (request.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && tank != null && tank instanceof IFluidHandler)
		{
			if (getTankInfo(tank)[0].fluid != null && FluidRegistry.getFluid(request.getItemDamage()) == getTankInfo(tank)[0].fluid.getFluid())
			{
				long drainedAmount = 0;

				for (long i = 0; i < request.getStackSize(); i++)
				{
					FluidStack drainedStack = ((IFluidHandler) tank).drain(facing, 1, true);
					if (drainedStack != null && drainedStack.amount != 0)
						drainedAmount += drainedStack.amount;
				}

				tank.onInventoryChanged();

				if (drainedAmount == 0)
				{
					return null;
				} else
				{
					removedStack.setStackSize(drainedAmount);
				}
				return removedStack;
			}
		}

		return null;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		try
		{
			if (tank != null && tank instanceof IFluidHandler)
			{
				if (getTankInfo(tank)[0].fluid != null && getTankInfo(tank)[0].fluid.getFluid() != null)
				{
					IAEItemStack currentItemStack = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, getTankInfo(tank)[0].fluid.getFluid().getID()));
					currentItemStack.setStackSize(getTankInfo(tank)[0].fluid.amount);
					out.add(currentItemStack);
				}

			}
		} catch (Throwable e)
		{
			System.out.println("I prevented a crash (I HOPE SO). Please send me the following error! ~M3gaF3ak/Leonelf");
			e.printStackTrace();
		}

		return out;
	}

	public boolean isItemInPreformattedItems(ItemStack request)
	{
		for (ItemStack itemstack : getPreformattedItems())
		{
			if (itemstack.getItem() == request.getItem() && itemstack.getItemDamage() == request.getItemDamage())
				return true;
		}
		return false;
	}

	@Override
	public IItemList getAvailableItems()
	{
		return getAvailableItems(Util.createItemList());

	}

	@Override
	public IAEItemStack calculateItemAddition(IAEItemStack input)
	{
		IAEItemStack addedStack = input.copy();

		if (input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (tank instanceof IFluidHandler)
			{

				if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid == null || FluidRegistry.getFluid(input.getItemDamage()) == ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid())
				{

					int filled = 0;

					for (long i = 0; i < input.getStackSize(); i++)
					{
						filled += ((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), 1), false);
					}

					addedStack.setStackSize(input.getStackSize() - filled);

					tank.onInventoryChanged();

					if (addedStack != null && addedStack.getStackSize() == 0)
						addedStack = null;

					return addedStack;
				}
			}
		}
		return addedStack;
	}

	@Override
	public long getAvailableSpaceByItem(IAEItemStack itemstack, long maxNeeded)
	{
		if (itemstack != null)
		{
			if (remainingItemCount() > 0)
			{
				return itemstack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() ? remainingItemCount() : 0;
			} else
			{
				for (IAEItemStack stack : this.getAvailableItems())
				{
					if (stack != null && stack.getItem() == itemstack.getItem() && stack.getItemDamage() == itemstack.getItemDamage())
						return remainingItemCount();
				}
			}
		}
		return 0;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public long totalBytes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return getTankInfo(tank) != null ? getTankInfo(tank)[0].capacity : 0;
		}
		return 0;
	}

	@Override
	public long freeBytes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return getTankInfo(tank)[0].fluid != null ? getTankInfo(tank)[0].capacity - getTankInfo(tank)[0].fluid.amount : getTankInfo(tank)[0].capacity;
		}
		return 0;
	}

	@Override
	public long usedBytes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{

			return getTankInfo(tank) != null ? getTankInfo(tank)[0].fluid.amount : 0;
		}
		return 0;
	}

	public FluidTankInfo[] getTankInfo(TileEntity tileEntity)
	{
		FluidTankInfo[] tankArray;
		IFluidHandler tankTile = (IFluidHandler) tileEntity;

		if (((IFluidHandler) tileEntity).getTankInfo(facing).length != 0)
		{
			return tankTile.getTankInfo(facing);
		} else if (tankTile.getTankInfo(ForgeDirection.UNKNOWN).length != 0)
		{
			return tankTile.getTankInfo(ForgeDirection.UNKNOWN);
		} else
		{
			return null;
		}
	}

	@Override
	public long unusedItemCount()
	{
		return freeBytes();
	}

	@Override
	public boolean canHoldNewItem()
	{
		return getAvailableItems().getItems().isEmpty();
	}

	@Override
	public void setUpdateTarget(TileEntity e)
	{
		this.updateTarget = e;
	}

	@Override
	public List<ItemStack> getPreformattedItems()
	{
		return filter;
	}

	@Override
	public boolean isPreformatted()
	{
		return !filter.isEmpty();
	}

	@Override
	public boolean isFuzzyPreformatted()
	{
		return false;
	}

	@Override
	public void setFuzzyPreformatted(boolean nf)
	{
		// Fuzzy on Fluids? I don't think so.
	}

	@Override
	public void setName(String name)
	{
		// A name for a Storagebus? NO!
	}

	@Override
	public String getName()
	{
		// A name for a Storagebus? NO!
		return "";
	}

	@Override
	public void setGrid(IGridInterface grid)
	{
		this.grid = grid;
	}

	@Override
	public IGridInterface getGrid()
	{
		return grid;
	}

	@Override
	public void setParent(IMEInventoryHandler parent)
	{
		this.parent = parent;

	}

	@Override
	public IMEInventoryHandler getParent()
	{
		return parent;
	}

	@Override
	public void removeGrid(IGridInterface grid, IMEInventoryHandler ignore, List<IMEInventoryHandler> duplicates)
	{
		// Algo told me to do nothing here :P
	}

	@Override
	public void validate(List<IMEInventoryHandler> duplicates)
	{
		// Algo told me to do nothing here :P
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		if (input != null && input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry())
		{
			if (getAvailableItems() != null)
			{
				for (IAEItemStack current : getAvailableItems())
				{
					if (current == null || current.getItemDamage() == input.getItemDamage())
						return true;
				}
				if (getAvailableItems().size() == 0)
					return true;
			} else
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public ItemFlow getFlow()
	{
		return ItemFlow.READ_WRITE;
	}

	@Override
	public void setFlow(ItemFlow p)
	{
		// Nothing
	}

	@Override
	public FuzzyMode getFuzzyModePreformatted()
	{
		return FuzzyMode.Percent_99;
	}

	@Override
	public void setPreformattedItems(IItemList in, FuzzyMode mode, ListMode m)
	{
		// Setting it in the Inventory
	}

	@Override
	public ListMode getListMode()
	{
		return ListMode.BLACKLIST;
	}
}
