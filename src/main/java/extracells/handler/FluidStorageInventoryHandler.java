package extracells.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.config.FuzzyMode;
import appeng.api.config.ItemFlow;
import appeng.api.config.ListMode;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.ItemEnum;

public class FluidStorageInventoryHandler implements IMEInventoryHandler
{
	public ItemStack storage;
	public long totalBytes;
	public int totalTypes;
	public int priority;
	public TileEntity updateTarget;
	public IGridInterface grid;
	public IMEInventoryHandler parent;

	public FluidStorageInventoryHandler(ItemStack itemstack, long totalBytes, int totalTypes)
	{
		this.storage = itemstack;
		this.totalBytes = totalBytes;
		this.totalTypes = totalTypes;
	}

	@Override
	public long storedItemTypes()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		int storedFluidTypes = 0;

		for (int i = 0; i < totalTypes; i++)
		{
			if (nbt.getInteger("FluidID#" + i) != 0)
				storedFluidTypes++;
		}
		return storedFluidTypes;
	}

	@Override
	public long storedItemCount()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		int storedFluidAmount = 0;

		for (int i = 0; i < totalTypes; i++)
		{
			storedFluidAmount += nbt.getLong("FluidAmount#" + i);
		}
		return storedFluidAmount;
	}

	@Override
	public long remainingItemCount()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		long remainingFluidSpace = totalBytes;

		for (int i = 0; i < totalTypes * 8000; i++)
		{
			remainingFluidSpace -= nbt.getLong("FluidAmount#" + i);
		}

		if (remainingFluidSpace < 0)
			remainingFluidSpace = 0;

		return remainingFluidSpace;
	}

	@Override
	public long remainingItemTypes()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		long remainingFluidTypes = totalBytes;

		for (int i = 0; i < totalTypes; i++)
		{
			if (nbt.getInteger("FluidID#" + i) == 0)
				remainingFluidTypes += 1;
		}

		return remainingFluidTypes;
	}

	@Override
	public boolean containsItemType(IAEItemStack aeitemstack)
	{
		if (aeitemstack.getItem() != ItemEnum.FLUIDDISPLAY.getItemEntry())
			return false;
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		long remainingFluidTypes = totalBytes;

		for (int i = 0; i < totalTypes; i++)
		{
			if (nbt.getInteger("FluidID#" + i) == aeitemstack.getItemDamage() && nbt.getLong("FluidAmount#" + i) > 0)
				return true;
		}

		return false;
	}

	@Override
	public long getTotalItemTypes()
	{
		return totalTypes;
	}

	@Override
	public long countOfItemType(IAEItemStack aeitemstack)
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		long countOfFluidType = 0;

		if (aeitemstack != null && aeitemstack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry())
		{
			for (int i = 0; i < totalTypes; i++)
			{
				if (nbt.getInteger("FluidID#" + i) == aeitemstack.getItemDamage())
					countOfFluidType += nbt.getLong("FluidAmount#" + i);
			}
		}
		return countOfFluidType;
	}

	@Override
	public IAEItemStack addItems(IAEItemStack input)
	{
		IAEItemStack addedStack = input.copy();

		if (input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (storage.stackTagCompound == null)
				storage.stackTagCompound = new NBTTagCompound();
			NBTTagCompound nbt = storage.stackTagCompound;

			for (int i = 0; i < totalTypes; i++)
			{
				if (input.getItemDamage() == nbt.getInteger("FluidID#" + i))
				{
					if (input.getStackSize() <= freeBytes())
					{
						nbt.setInteger("FluidID#" + i, input.getItemDamage());
						nbt.setLong("FluidAmount#" + i, nbt.getLong("FluidAmount#" + i) + input.getStackSize());
						addedStack = null;
					} else
					{
						nbt.setInteger("FluidID#" + i, input.getItemDamage());
						nbt.setLong("FluidAmount#" + i, nbt.getLong("FluidAmount#" + i) + freeBytes());
						addedStack.setStackSize(input.getStackSize() - freeBytes());
					}
					return addedStack;
				}
			}

			for (int i = 0; i < totalTypes; i++)
			{
				if (nbt.getInteger("FluidID#" + i) == 0)
				{
					if (input.getStackSize() <= freeBytes())
					{
						nbt.setInteger("FluidID#" + i, input.getItemDamage());
						nbt.setLong("FluidAmount#" + i, nbt.getLong("FluidAmount#" + i) + input.getStackSize());
						addedStack = null;
					} else
					{
						nbt.setInteger("FluidID#" + i, input.getItemDamage());
						nbt.setLong("FluidAmount#" + i, nbt.getLong("FluidAmount#" + i) + freeBytes());
						addedStack.setStackSize(input.getStackSize() - freeBytes());
					}
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

		if (request.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry())
		{
			if (storage.stackTagCompound == null)
				storage.stackTagCompound = new NBTTagCompound();
			NBTTagCompound nbt = storage.stackTagCompound;

			for (int i = 0; i < totalTypes; i++)
			{
				if (request.getItemDamage() == nbt.getInteger("FluidID#" + i))
				{
					if (nbt.getLong("FluidAmount#" + i) - request.getStackSize() >= 0)
					{
						removedStack.setStackSize(request.getStackSize());
						if (nbt.getLong("FluidAmount#" + i) - request.getStackSize() == 0)
							nbt.setInteger("FluidID#" + i, 0);
						nbt.setLong("FluidAmount#" + i, nbt.getLong("FluidAmount#" + i) - request.getStackSize());
					} else
					{
						removedStack.setStackSize(nbt.getLong("FluidAmount#" + i));
						nbt.setInteger("FluidID#" + i, 0);
						nbt.setLong("FluidAmount#" + i, 0);
					}

					if (updateTarget != null)
						updateTarget.onInventoryChanged();

					return removedStack;
				}
			}
		}
		return null;
	}

	@Override
	public IItemList getAvailableItems(IItemList out)
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		for (int i = 0; i < totalTypes; i++)
		{
			if (nbt.getInteger("FluidID#" + i) != 0 && nbt.getLong("FluidAmount#" + i) != 0)
			{
				IAEItemStack currentItemStack = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, nbt.getInteger("FluidID#" + i)));
				currentItemStack.setStackSize(nbt.getLong("FluidAmount#" + i));
				out.add(currentItemStack);
			}
		}

		return out;
	}

	@Override
	public IItemList getAvailableItems()
	{
		return getAvailableItems(Util.createItemList());

	}

	@Override
	public IAEItemStack calculateItemAddition(IAEItemStack input)
	{
		if (input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			if (storage.stackTagCompound == null)
				storage.stackTagCompound = new NBTTagCompound();
			NBTTagCompound nbt = storage.stackTagCompound;

			IAEItemStack addedStack = input.copy();

			for (int i = 0; i < totalTypes; i++)
			{
				if (nbt.getInteger("FluidID#" + i) == 0 || input.getItemDamage() == nbt.getInteger("FluidID#" + i))
				{
					if (input.getStackSize() <= freeBytes())
					{
						addedStack = null;
					} else
					{
						addedStack.setStackSize(input.getStackSize() - freeBytes());
					}
					return addedStack;
				}
			}
		}
		return input;
	}

	public boolean isItemInPreformattedItems(ItemStack request)
	{
		if (!isPreformatted())
			return true;

		for (ItemStack itemstack : getPreformattedItems())
		{
			if (itemstack.getItem() == request.getItem() && itemstack.getItemDamage() == request.getItemDamage())
				return true;
		}
		return false;
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
		return totalBytes;
	}

	@Override
	public long freeBytes()
	{
		return remainingItemCount();
	}

	@Override
	public long usedBytes()
	{
		return totalBytes() - freeBytes();
	}

	@Override
	public long unusedItemCount()
	{
		return freeBytes();
	}

	@Override
	public boolean canHoldNewItem()
	{
		IItemList fluidItemList = Util.createItemList();
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		for (int i = 0; i < totalTypes; i++)
		{
			if (nbt.getInteger("FluidID#" + i) == 0 && nbt.getLong("FluidAmount#" + i) == 0)
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public void setUpdateTarget(TileEntity e)
	{
		this.updateTarget = e;
	}

	@Override
	public List<ItemStack> getPreformattedItems()
	{
		List<ItemStack> fluidItemList = new ArrayList<ItemStack>();
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		for (int i = 0; i < 63; i++)
		{
			if (nbt.getInteger("PreformattedFluidID#" + i) != 0)
			{
				fluidItemList.add(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, nbt.getInteger("PreformattedFluidID#" + i)));
			}
		}

		return fluidItemList;
	}

	@Override
	public boolean isPreformatted()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		for (int i = 0; i < 63; i++)
		{
			if (nbt.getInteger("PreformattedFluidID#" + i) != 0)
				return true;
		}
		return false;
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
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		nbt.setString("PreformattedName", name);

	}

	@Override
	public String getName()
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		return nbt.getString("PreformattedName");
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
		if (input != null)
		{
			if (getAvailableItems() != null)
			{
				for (IAEItemStack current : getAvailableItems())
				{
					if (current == null || current.getItemDamage() == input.getItemDamage())
						return true;
				}
			} else
			{
				return input.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry();
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
		// nothing
	}

	@Override
	public FuzzyMode getFuzzyModePreformatted()
	{
		return FuzzyMode.Percent_99;
	}

	@Override
	public void setPreformattedItems(IItemList in, FuzzyMode mode, ListMode m)
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		if (!in.getItems().isEmpty())
		{
			for (int i = 0; i < in.getItems().size(); i++)
			{
				ItemStack currentItemStack = in.getItems().get(i);
				if (currentItemStack != null)
				{
					if (FluidContainerRegistry.isFilledContainer(currentItemStack))
					{
						nbt.setInteger("PreformattedFluidID#" + i, FluidContainerRegistry.getFluidForFilledItem(currentItemStack).fluidID);
					} else if (currentItemStack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry())
					{
						nbt.setInteger("PreformattedFluidID#" + i, currentItemStack.getItemDamage());
					} else if (currentItemStack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) currentItemStack.getItem()).getFluid(currentItemStack) != null)
					{
						nbt.setInteger("PreformattedFluidID#" + i, ((IFluidContainerItem) currentItemStack.getItem()).getFluid(currentItemStack).fluidID);
					}
				}
			}
		} else
		{
			for (int i = 0; i < 63; i++)
			{
				nbt.setInteger("PreformattedFluidID#" + i, 0);
			}
		}
	}

	@Override
	public ListMode getListMode()
	{
		return ListMode.BLACKLIST;
	}
}