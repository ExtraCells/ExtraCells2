package extracells.tileentity;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.ICraftingTracker;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.ICraftingPattern;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.ITileCraftingProvider;
import extracells.ItemEnum;
import extracells.util.ECPrivateInventory;
import extracells.util.ECPrivatePatternInventory;

public class TileEntityFluidCrafter extends TileEntity implements ITileCraftingProvider, IGridMachine
{
	private List<ICraftingPattern> patternHandlers = new ArrayList<ICraftingPattern>();
	private ItemStack currentRequest = null;
	private ECPrivatePatternInventory patternInventory = new ECPrivatePatternInventory("", 9, 64, this);
	private ECPrivateInventory bufferInventory = new ECPrivateInventory("", 18, 10000);
	private IGridInterface grid;
	private boolean powerStatus = false, networkStatus = true;

	public void updateEntity()
	{
		if (grid != null && isMachineActive() && grid.getCellArray() != null)
		{
			for (ICraftingPattern pattern : patternHandlers)
			{
				if (itemsForPatternExist(pattern))
				{
					for (ItemStack stack : pattern.getRequirements())
					{
						removeItems(stack);
					}
					grid.getCellArray().addItems(Util.createItemStack(pattern.getOutput()));
				}
			}
		}
	}

	public void removeItems(ItemStack stack)
	{
		for (int i = 0; i < bufferInventory.slots.size(); i++)
		{
			ItemStack item = bufferInventory.getStackInSlot(i) != null ? bufferInventory.getStackInSlot(i).copy() : null;

			if (item != null && item.isItemEqual(stack) && ItemStack.areItemStackTagsEqual(stack, item))
			{
				if (item.stackSize > stack.stackSize)
				{
					item.stackSize -= stack.stackSize;
					bufferInventory.slots.set(i, item);
				} else if (item.stackSize == stack.stackSize)
				{
					bufferInventory.slots.set(i, null);
				}
			}
		}
	}

	public boolean itemsForPatternExist(ICraftingPattern pattern)
	{
		for (ItemStack stack : pattern.getRequirements())
		{
			if (!itemContainedInInventory(stack, pattern))
				return false;
		}
		return true;
	}

	public boolean itemContainedInInventory(ItemStack items, ICraftingPattern pattern)
	{
		for (ItemStack stack : bufferInventory.slots)
		{
			if (stack != null && stack.isItemEqual(items) && ItemStack.areItemStackTagsEqual(items, stack) && stack.stackSize >= items.stackSize)
			{
				return true;
			}
		}
		return false;
	}

	@Override
	public void validate()
	{
		super.validate();
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
	}

	@Override
	public boolean isBusy()
	{
		return currentRequest != null;
	}

	@Override
	public ItemStack pushItem(ItemStack out)
	{
		if (canPushItem(out))
		{
			ItemStack rejected = null;
			for (int i = 0; i < bufferInventory.slots.size(); i++)
			{
				ItemStack stack = bufferInventory.getStackInSlot(i) != null ? bufferInventory.getStackInSlot(i).copy() : null;
				if (stack == null)
				{
					bufferInventory.slots.set(i, out);
					return rejected;
				} else if (stack.isItemEqual(out) && ItemStack.areItemStackTagsEqual(out, stack))
				{
					stack.stackSize += out.stackSize;
					bufferInventory.slots.set(i, stack);
					return rejected;
				}
			}
		}
		return out;
	}

	@Override
	public boolean canPushItem(ItemStack out)
	{
		return out != null;
	}

	@Override
	public void provideCrafting(ICraftingTracker craftingTracker)
	{
		patternHandlers = new ArrayList<ICraftingPattern>();

		for (ItemStack currentPatternStack : patternInventory.slots)
		{
			if (currentPatternStack != null)
			{
				ICraftingPattern currentPattern = Util.getAssemblerPattern(currentPatternStack);

				if (currentPattern != null)
				{
					convertToRequirementsToFluid(currentPattern.getRequirements());
					patternHandlers.add(currentPattern);
					craftingTracker.addCraftingOption(this, currentPattern);
				}
			}
		}
	}

	public void convertToRequirementsToFluid(List<ItemStack> requirements)
	{
		List<ItemStack> toRemove = new ArrayList<ItemStack>();
		List<ItemStack> toAdd = new ArrayList<ItemStack>();

		for (ItemStack currentRequirement : requirements)
		{
			if (currentRequirement != null)
			{
				FluidStack fluid = null;
				if (FluidContainerRegistry.isFilledContainer(currentRequirement))
				{
					fluid = FluidContainerRegistry.getFluidForFilledItem(currentRequirement);
				} else if (currentRequirement.getItem() instanceof IFluidContainerItem)
				{
					fluid = ((IFluidContainerItem) currentRequirement.getItem()).getFluid(currentRequirement);
				}
				if (fluid != null)
				{
					toAdd.add(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), fluid.amount, fluid.fluidID));
					toRemove.add(currentRequirement);
				}
			}
		}
		for (ItemStack currentRequirement : toRemove)
		{
			requirements.remove(currentRequirement);
		}
		for (ItemStack currentRequirement : toAdd)
		{
			requirements.add(currentRequirement);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setTag("BufferItems", bufferInventory.writeToNBT());
		nbt.setTag("InventoryItems", patternInventory.writeToNBT());
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("BufferItemsName", bufferInventory.customName);
			nbt.setString("InventoryItemsName", patternInventory.customName);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList bufferList = nbt.getTagList("BufferItems");
		NBTTagList patternList = nbt.getTagList("InventoryItems");
		bufferInventory.readFromNBT(bufferList);
		patternInventory.readFromNBT(patternList);
		if (nbt.hasKey("BufferItemsName"))
		{
			bufferInventory.customName = nbt.getString("BufferItemsName");
		}
		if (nbt.hasKey("InventoryItemsName"))
		{
			patternInventory.customName = nbt.getString("InventoryItemsName");
		}
	}

	public ECPrivateInventory getInventory()
	{
		return patternInventory;
	}

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower)
	{
		powerStatus = hasPower;
	}

	@Override
	public boolean isPowered()
	{
		return powerStatus;
	}

	@Override
	public IGridInterface getGrid()
	{
		return grid;
	}

	@Override
	public void setGrid(IGridInterface gi)
	{
		grid = gi;
	}

	@Override
	public World getWorld()
	{
		return worldObj;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 10.0F;
	}

	@Override
	public void setNetworkReady(boolean isReady)
	{
		networkStatus = isReady;
	}

	@Override
	public boolean isMachineActive()
	{
		return networkStatus && powerStatus;
	}
}
