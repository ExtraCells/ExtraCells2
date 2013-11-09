package extracells.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
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
	private ItemStack storage;
	private long totalBytes;
	private int totalTypes;
	private int priority;
	private TileEntity updateTarget;
	private IGridInterface grid;
	private IMEInventoryHandler parent;
	private FluidStack[] cachedInventory;
	private String cachedName;
	private Item fluidItem = ItemEnum.FLUIDDISPLAY.getItemEntry();

	public FluidStorageInventoryHandler(ItemStack itemstack, long totalBytes, int totalTypes)
	{
		this.storage = itemstack;
		this.totalBytes = totalBytes;
		this.totalTypes = totalTypes;
		cachedInventory = new FluidStack[totalTypes];

		for (int i = 0; i < totalTypes; i++)
		{
			cachedInventory[i] = readFluidStackFromSlot(i);
		}

		cachedName = readNameFromNBT();
	}

	@Override
	public long storedItemTypes()
	{
		int storedFluidTypes = 0;

		for (int i = 0; i < totalTypes; i++)
		{
			if (cachedInventory[i] != null)
				storedFluidTypes++;
		}

		return storedFluidTypes;
	}

	@Override
	public long storedItemCount()
	{
		int storedFluidAmount = 0;

		for (int i = 0; i < totalTypes; i++)
		{
			FluidStack currentStack = cachedInventory[i];
			storedFluidAmount += currentStack != null ? currentStack.amount : 0;
		}

		return storedFluidAmount;
	}

	@Override
	public long remainingItemCount()
	{
		long remainingFluidSpace = totalBytes;

		for (int i = 0; i < totalTypes; i++)
		{
			FluidStack currentStack = cachedInventory[i];
			remainingFluidSpace -= currentStack != null ? currentStack.amount : 0;
		}

		return remainingFluidSpace < 0 ? 0 : remainingFluidSpace;
	}

	@Override
	public long remainingItemTypes()
	{
		long remainingFluidTypes = totalBytes;

		for (int i = 0; i < totalTypes; i++)
		{
			if (cachedInventory[i] == null)
				remainingFluidTypes += 1;
		}

		return remainingFluidTypes;
	}

	@Override
	public boolean containsItemType(IAEItemStack aeitemstack)
	{
		long remainingFluidTypes = totalBytes;

		if (aeitemstack != null && aeitemstack.getItem() == fluidItem)
		{
			for (int i = 0; i < totalTypes; i++)
			{
				FluidStack currentStack = cachedInventory[i];
				if (currentStack != null && currentStack.fluidID == aeitemstack.getItemDamage())
					return true;
			}
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
		long countOfFluidType = 0;

		if (aeitemstack != null && aeitemstack.getItem() == fluidItem)
		{
			for (int i = 0; i < totalTypes; i++)
			{
				FluidStack currentStack = cachedInventory[i];
				if (currentStack != null && currentStack.fluidID == aeitemstack.getItemDamage())
					countOfFluidType += currentStack.amount;
			}
		}
		return countOfFluidType;
	}

	@Override
	public IAEItemStack addItems(IAEItemStack input)
	{
		IAEItemStack addedStack = input.copy();

		if (input.getItem() == fluidItem && (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack()))))
		{
			for (int i = 0; i < totalTypes; i++)
			{
				FluidStack currentStack = cachedInventory[i];
				if (currentStack != null && currentStack.fluidID == input.getItemDamage())
				{
					if (input.getStackSize() <= freeBytes())
					{
						writeFluidStackToSlot(i, new FluidStack(input.getItemDamage(), currentStack.amount + (int) input.getStackSize()));
						addedStack = null;
					} else
					{
						writeFluidStackToSlot(i, new FluidStack(input.getItemDamage(), currentStack.amount + (int) freeBytes()));
						addedStack.setStackSize(input.getStackSize() - freeBytes());
					}
					return addedStack;
				}
			}

			for (int i = 0; i < totalTypes; i++)
			{
				FluidStack currentStack = cachedInventory[i];
				if (currentStack == null)
				{
					if (input.getStackSize() <= freeBytes())
					{
						writeFluidStackToSlot(i, new FluidStack(input.getItemDamage(), (int) input.getStackSize()));
						addedStack = null;
					} else
					{
						writeFluidStackToSlot(i, new FluidStack(input.getItemDamage(), (int) freeBytes()));
						addedStack.setStackSize(input.getStackSize() - freeBytes());
					}
					return addedStack;
				}
			}
		}
		return addedStack;
	}

	@Override
	public IAEItemStack calculateItemAddition(IAEItemStack input)
	{
		if (input.getItem() == fluidItem)
		{
			if (!isPreformatted() || (isPreformatted() && isItemInPreformattedItems(input.getItemStack())))
			{
				IAEItemStack addedStack = input.copy();

				for (int i = 0; i < totalTypes; i++)
				{
					FluidStack currentStack = cachedInventory[i];
					if (currentStack == null || currentStack.fluidID == input.getItemDamage())
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
		}
		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request)
	{
		IAEItemStack removedStack = request.copy();

		if (request.getItem() == fluidItem)
		{
			for (int i = 0; i < totalTypes; i++)
			{
				FluidStack currentStack = cachedInventory[i];
				if (currentStack != null && currentStack.fluidID == request.getItemDamage())
				{
					if (currentStack.amount - request.getStackSize() >= 0)
					{
						removedStack.setStackSize(request.getStackSize());
						if (currentStack.amount - request.getStackSize() == 0)
						{
							writeFluidStackToSlot(i, null);
						} else
						{
							writeFluidStackToSlot(i, new FluidStack(currentStack.fluidID, currentStack.amount - (int) request.getStackSize()));
						}
					} else
					{
						removedStack.setStackSize(currentStack.amount);
						writeFluidStackToSlot(i, null);
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
		for (int i = 0; i < totalTypes; i++)
		{
			FluidStack currentStack = cachedInventory[i];
			if (currentStack != null)
			{
				IAEItemStack currentItemStack = Util.createItemStack(new ItemStack(fluidItem, 1, currentStack.fluidID));
				currentItemStack.setStackSize(currentStack.amount);
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
				return itemstack.getItem() == fluidItem ? remainingItemCount() : 0;
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
		for (int i = 0; i < totalTypes; i++)
		{
			if (cachedInventory[i] == null)
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
		writeNameToNBT(name);
		cachedName = name;
	}

	@Override
	public String getName()
	{
		return readNameFromNBT();
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
		if (input != null && input.getItem() == fluidItem)
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

	private void writeFluidStackToSlot(int slotID, FluidStack input)
	{
		cachedInventory[slotID] = input;

		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		nbt.setInteger("FluidID#" + slotID, input != null ? input.fluidID : -1);
		nbt.setLong("FluidAmount#" + slotID, input != null ? input.amount : -1);
	}

	private FluidStack readFluidStackFromSlot(int slotID)
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		return nbt.getInteger("FluidID#" + slotID) > 0 && nbt.getLong("FluidAmount#" + slotID) > 0 ? new FluidStack(nbt.getInteger("FluidID#" + slotID), (int) nbt.getLong("FluidAmount#" + slotID)) : null;
	}

	private void writeNameToNBT(String name)
	{
		if (storage.stackTagCompound == null)
			storage.stackTagCompound = new NBTTagCompound();
		NBTTagCompound nbt = storage.stackTagCompound;

		nbt.setString("PreformattedName", name);
	}

	private String readNameFromNBT()
	{
		if (storage.stackTagCompound == null)
			storage.setTagCompound(new NBTTagCompound());
		NBTTagCompound nbt = storage.stackTagCompound;

		return nbt.getString("PreformattedName");
	}
}