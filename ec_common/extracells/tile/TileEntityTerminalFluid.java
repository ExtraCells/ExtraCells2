package extracells.tile;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.util.IGridInterface;

public class TileEntityTerminalFluid extends TileEntity implements IGridMachine, IDirectionalMETile, IInventory, IStorageAware
{
	Boolean powerStatus;
	IGridInterface grid;
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.terminal");
	private ItemStack[] slots = new ItemStack[3];
	private int fluidIndex = 0;
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();

	public TileEntityTerminalFluid()
	{
		updateFluids();
	}

	public void updateEntity()
	{
		if (powerStatus == null)
			powerStatus = true;
		
		if (!worldObj.isRemote && powerStatus)
		{
			ItemStack input = this.getStackInSlot(0);
			ItemStack output = this.getStackInSlot(1);

			if (!fluidsInNetwork.isEmpty())
			{
				try
				{
					fluidsInNetwork.get(fluidIndex);
				} catch (IndexOutOfBoundsException e)
				{
					fluidIndex = 0;
				}

				FluidStack request = new FluidStack((fluidsInNetwork.get(fluidIndex).getFluid()), 1000);
				ItemStack preview = new ItemStack(extracells.Extracells.FluidDisplay, 1, fluidsInNetwork.get(fluidIndex).getID());

				if (preview.getTagCompound() == null)
					preview.setTagCompound(new NBTTagCompound());
				NBTTagCompound nbt = preview.getTagCompound();
				nbt.setLong("amount", fluidsInNetwork.get(fluidIndex).amount);
				nbt.setString("fluidname", capitalizeFirstLetter(fluidsInNetwork.get(fluidIndex).fluid.getName()));

				this.setInventorySlotContents(2, preview);

				if (input != null)
				{
					if (FluidContainerRegistry.isEmptyContainer(input))
					{
						if (FluidContainerRegistry.fillFluidContainer(request, input) != null)
						{
							if (output == null)
							{
								if (drainFluid(request))
								{
									this.setInventorySlotContents(1, FluidContainerRegistry.fillFluidContainer(request, input));
									this.decrStackSize(0, 1);
								}
							} else if (output.isStackable() && output.stackSize < output.getMaxStackSize() && output.getItem() == FluidContainerRegistry.fillFluidContainer(request, input).getItem())
							{
								if (drainFluid(request))
								{
									output.stackSize = output.stackSize + 1;
									this.decrStackSize(0, 1);
								}
							}
						}
					}
				}
			} else
			{
				this.setInventorySlotContents(2, null);
			}

			if (FluidContainerRegistry.isFilledContainer(input))
			{
				ItemStack drainedContainer = input.getItem().getContainerItemStack(input);
				FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(input);

				if (FluidContainerRegistry.getFluidForFilledItem(input) != null && fillFluid(FluidContainerRegistry.getFluidForFilledItem(input)))
				{
					if (output == null)
					{
						this.setInventorySlotContents(1, drainedContainer);
						this.decrStackSize(0, 1);
					} else if (output.isStackable() && output.stackSize < output.getMaxStackSize())
					{
						if (drainedContainer == null)
						{
							this.decrStackSize(0, 1);
						} else if (output.getItem() == drainedContainer.getItem())
						{
							output.stackSize = output.stackSize + 1;
							this.decrStackSize(0, 1);
						}
					}
				}
			}
		}
	}

	// FluidStack with long amount :D
	class SpecialFluidStack
	{
		long amount;
		Fluid fluid;

		public SpecialFluidStack(Fluid fluid, long amount)
		{
			this.fluid = fluid;
			this.amount = amount;
		}

		public SpecialFluidStack(int id, long amount)
		{
			this.fluid = FluidRegistry.getFluid(id);
			this.amount = amount;
		}

		public long getAmount()
		{
			return amount;
		}

		public Fluid getFluid()
		{
			return fluid;
		}

		public int getID()
		{
			return fluid.getID();
		}
	}

	public void updateFluids()
	{
		fluidsInNetwork = new ArrayList<SpecialFluidStack>();

		if (grid != null)
		{
			IItemList itemsInNetwork = grid.getCellArray().getAvailableItems();

			for (IAEItemStack itemstack : itemsInNetwork)
			{
				if (itemstack.getItem() == extracells.Extracells.FluidDisplay)
				{
					fluidsInNetwork.add(new SpecialFluidStack(itemstack.getItemDamage(), itemstack.getStackSize()));
				}
			}
		}
	}

	public boolean fillFluid(FluidStack toImport)
	{
		IAEItemStack toFill = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, toImport.amount, toImport.fluidID));
		if (grid != null)
		{
			if (grid.getCellArray().calculateItemAddition(toFill) == null)
			{
				for (int i = 0; i < toFill.getStackSize() / 10; i++)
				{
					grid.getCellArray().addItems(Util.createItemStack(new ItemStack(toFill.getItem(), 10, toFill.getItemDamage())));
				}
				return true;
			}
		}
		return false;
	}

	public boolean drainFluid(FluidStack toExport)
	{
		IAEItemStack toDrain = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, toExport.amount, toExport.fluidID));
		if (grid != null)
		{
			for (SpecialFluidStack fluidstack : fluidsInNetwork)
			{
				if (fluidstack.getFluid() == toExport.getFluid() && fluidstack.amount >= toExport.amount)
				{
					for (int i = 0; i < toDrain.getStackSize() / 10; i++)
					{
						grid.getCellArray().extractItems(Util.createItemStack(new ItemStack(toDrain.getItem(), 10, toDrain.getItemDamage())));
					}

					return true;
				}
			}
		}
		return false;
	}

	public void setCurrentFluid(int modifier)
	{
		if (fluidIndex + modifier >= 0)
			this.fluidIndex = fluidIndex + modifier;
	}

	public int getCurrentFluid()
	{
		return fluidIndex;
	}

	public String capitalizeFirstLetter(String original)
	{
		if (original.length() == 0)
			return original;
		return original.substring(0, 1).toUpperCase() + original.substring(1);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.slots.length; ++i)
		{
			if (this.slots[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.slots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbt.setTag("Items", nbttaglist);
		if (this.isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		this.slots = new ItemStack[this.getSizeInventory()];
		if (nbt.hasKey("CustomName"))
		{
			this.costumName = nbt.getString("CustomName");
		}
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < this.slots.length)
			{
				this.slots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
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
	public boolean canConnect(ForgeDirection dir)
	{
		return dir.ordinal() != this.blockMetadata;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 5.0F;
	}

	@Override
	public int getSizeInventory()
	{
		return slots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return slots[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.slots[i] != null)
		{
			ItemStack itemstack;
			if (this.slots[i].stackSize <= j)
			{
				itemstack = this.slots[i];
				this.slots[i] = null;
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				itemstack = this.slots[i].splitStack(j);
				if (this.slots[i].stackSize == 0)
				{
					this.slots[i] = null;
				}
				this.onInventoryChanged();
				return itemstack;
			}
		} else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (this.slots[i] != null)
		{
			ItemStack itemstack = this.slots[i];
			this.slots[i] = null;
			return itemstack;
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.slots[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	@Override
	public String getInvName()
	{
		return costumName;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
		// NOBODY needs this!
	}

	@Override
	public void closeChest()
	{
		// NOBODY needs this!
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return FluidContainerRegistry.isContainer(itemstack);
	}

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		updateFluids();
	}
}
