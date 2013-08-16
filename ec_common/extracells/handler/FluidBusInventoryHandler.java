package extracells.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.config.FuzzyMode;
import appeng.api.config.ItemFlow;
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
			return ((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null ? 1 : 0;
		}
		return 0;
	}

	@Override
	public long storedItemCount()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount;
		}
		return 0;
	}

	@Override
	public long remainingItemCount()
	{
		if (tank != null && tank instanceof IFluidHandler && ((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null)
		{
			return ((IFluidHandler) tank).getTankInfo(facing)[0].capacity - ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount;
		}
		return 0;
	}

	@Override
	public long remainingItemTypes()
	{
		if (tank != null && tank instanceof IFluidHandler && ((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null)
		{
			return 1;
		}
		return 0;
	}

	@Override
	public boolean containsItemType(IAEItemStack aeitemstack)
	{
		return aeitemstack.getItem() == extracells.Extracells.FluidDisplay;
	}

	@Override
	public long getTotalItemTypes()
	{
		return 1;
	}

	@Override
	public long countOfItemType(IAEItemStack aeitemstack)
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return aeitemstack.getItem() == extracells.Extracells.FluidDisplay ? ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount : 0;
		}
		return 0;
	}

	@Override
	public IAEItemStack addItems(IAEItemStack input)
	{
		IAEItemStack addedStack = input.copy();

		if (input.getItem() == extracells.Extracells.FluidDisplay && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (tank instanceof IFluidHandler)
			{
				if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid == null || FluidRegistry.getFluid(input.getItemDamage()) == ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid())
				{
					if (input.getStackSize() <= freeBytes())
					{
						addedStack = null;
						((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), (int) input.getStackSize()), true);
					} else
					{
						addedStack.setStackSize(input.getStackSize() - freeBytes());
						((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), (int) (totalBytes() - freeBytes())), true);
					}

					tank.onInventoryChanged();

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

		if (request.getItem() == extracells.Extracells.FluidDisplay && tank != null && tank instanceof IFluidHandler)
		{
			if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null && FluidRegistry.getFluid(request.getItemDamage()) == ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid())
			{
				if (request.getStackSize() <= usedBytes())
				{
					removedStack.setStackSize(request.getStackSize());
					((IFluidHandler) tank).drain(facing, new FluidStack(request.getItemDamage(), (int) request.getStackSize()), true);
				} else
				{
					removedStack.setStackSize(usedBytes());
					((IFluidHandler) tank).drain(facing, new FluidStack(request.getItemDamage(), (int) usedBytes()), true);
				}

				tank.onInventoryChanged();

				return removedStack;
			}
		}
		return null;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null)
			{
				IAEItemStack currentItemStack = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 0, ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid().getID()));
				currentItemStack.setStackSize(((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount);
				out.add(currentItemStack);
			}

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

		if (input.getItem() == extracells.Extracells.FluidDisplay && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (tank instanceof IFluidHandler)
			{
				if (((IFluidHandler) tank).getTankInfo(facing)[0].fluid == null || FluidRegistry.getFluid(input.getItemDamage()) == ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.getFluid())
				{
					if (input.getStackSize() <= freeBytes())
					{
						addedStack = null;
						((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), (int) input.getStackSize()), false);
					} else
					{
						addedStack.setStackSize(input.getStackSize() - freeBytes());
						((IFluidHandler) tank).fill(facing, new FluidStack(input.getItemDamage(), (int) (totalBytes() - freeBytes())), false);
					}

					tank.onInventoryChanged();

					return addedStack;
				}
			}
		}
		return addedStack;
	}

	@Override
	public long getAvailableSpaceByItem(IAEItemStack itemstack, long maxNeeded)
	{
		return itemstack.getItem() == extracells.Extracells.FluidDisplay ? remainingItemCount() : 0;
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
			return ((IFluidHandler) tank).getTankInfo(facing)[0].capacity;
		}
		return 0;
	}

	@Override
	public long freeBytes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return ((IFluidHandler) tank).getTankInfo(facing)[0].fluid != null ? ((IFluidHandler) tank).getTankInfo(facing)[0].capacity - ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount : ((IFluidHandler) tank).getTankInfo(facing)[0].capacity;
		}
		return 0;
	}

	@Override
	public long usedBytes()
	{
		if (tank != null && tank instanceof IFluidHandler)
		{
			return ((IFluidHandler) tank).getTankInfo(facing)[0].fluid.amount;
		}
		return 0;
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
	public void setPreformattedItems(IItemList in, FuzzyMode fuzzyMode)
	{
		// I set it in the GUI
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
		return input.getItem() == extracells.Extracells.FluidDisplay;
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

}
