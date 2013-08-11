package extracells.tile;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.TileRef;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.exceptions.AppEngTileMissingException;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;

public class TileEntityTerminalFluid extends TileEntity implements IGridMachine, IDirectionalMETile, IInventory
{
	Boolean powerStatus = false;
	IGridInterface grid;
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.terminal");
	private ItemStack[] slots = new ItemStack[3];
	private int fluidIndex = 0;
	ArrayList<FluidStack> fluidStacksInNetwork = new ArrayList<FluidStack>();

	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			ItemStack input = this.getStackInSlot(0);
			ItemStack output = this.getStackInSlot(1);

			updateFluids();

			if (!fluidStacksInNetwork.isEmpty())
			{
				try
				{
					fluidStacksInNetwork.get(fluidIndex);
				} catch (IndexOutOfBoundsException e)
				{
					fluidIndex = 0;
				}

				FluidStack request = new FluidStack(((FluidStack) fluidStacksInNetwork.toArray()[fluidIndex]).getFluid(), 1000);

				ItemStack preview = new ItemStack(extracells.Extracells.FluidDisplay, 1, ((FluidStack) fluidStacksInNetwork.toArray()[fluidIndex]).fluidID);

				fluidStacksInNetwork.get(fluidIndex);
				if (preview.stackTagCompound == null)
					preview.stackTagCompound = new NBTTagCompound();
				preview.stackTagCompound.setInteger("amount", ((FluidStack) fluidStacksInNetwork.toArray()[fluidIndex]).amount);
				preview.stackTagCompound.setString("fluidname", capitalizeFirstLetter(((FluidStack) fluidStacksInNetwork.toArray()[fluidIndex]).getFluid().getName()));
				preview.stackTagCompound.setInteger("fluidID", ((FluidStack) fluidStacksInNetwork.toArray()[fluidIndex]).fluidID);
				this.setInventorySlotContents(2, null);
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

				if (fillFluid(FluidContainerRegistry.getFluidForFilledItem(input)))
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

	public boolean fillFluid(FluidStack toFill)
	{
		if (grid != null)
		{
			List<TileRef<IGridMachine>> tilelist = grid.getMachines();

			for (TileRef<IGridMachine> entry : tilelist)
			{
				try
				{
					if (entry.getTile() instanceof TileEntityBusFluidStorage)
					{
						TileEntityBusFluidStorage storageBus = (TileEntityBusFluidStorage) entry.getTile();
						ForgeDirection busFacing = storageBus.getFacing().getOpposite();

						IFluidHandler tank = storageBus.getTank();
						if (tank != null)
						{
							FluidTankInfo[] tankInfoArray = storageBus.getTank().getTankInfo(busFacing);
							if (tankInfoArray != null)
							{
								FluidTankInfo tankInfo = storageBus.getTank().getTankInfo(busFacing)[0];

								if (tankInfo != null)
								{
									FluidStack tankFluid = storageBus.getTank().getTankInfo(busFacing)[0].fluid;
									int capacity = storageBus.getTank().getTankInfo(busFacing)[0].capacity;

									if (storageBus.getTank().fill(busFacing, toFill, false) == toFill.amount)
									{
										storageBus.getTank().fill(busFacing, toFill, true);
										return true;
									}
								}
							}
						}
					}
				} catch (AppEngTileMissingException e)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public boolean drainFluid(FluidStack toDrain)
	{
		if (grid != null)
		{
			List<TileRef<IGridMachine>> tilelist = grid.getMachines();

			for (TileRef<IGridMachine> entry : tilelist)
			{
				try
				{
					if (entry.getTile() instanceof TileEntityBusFluidStorage)
					{
						TileEntityBusFluidStorage storageBus = (TileEntityBusFluidStorage) entry.getTile();
						ForgeDirection busFacing = storageBus.getFacing().getOpposite();

						IFluidHandler tank = storageBus.getTank();
						if (tank != null)
						{
							FluidTankInfo[] tankInfoArray = storageBus.getTank().getTankInfo(busFacing);
							if (tankInfoArray != null)
							{
								FluidTankInfo tankInfo = tankInfoArray[0];

								if (tankInfo != null)
								{
									FluidStack tankFluid = tankInfo.fluid;
									int capacity = tankInfo.capacity;

									if (tank.drain(busFacing, toDrain, false) != null && storageBus.getTank().drain(busFacing, toDrain, false).amount == toDrain.amount)
									{
										tank.drain(busFacing, toDrain, true);
										return true;
									}
								}
							}
						}
					}
				} catch (AppEngTileMissingException e)
				{
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public void updateFluids()
	{
		if (grid != null)
		{
			fluidStacksInNetwork = new ArrayList<FluidStack>();
			List<TileRef<IGridMachine>> tilelist = grid.getMachines();

			for (TileRef<IGridMachine> entry : tilelist)
			{
				if (grid != null)
				{
					try
					{
						if (entry.getTile() instanceof TileEntityBusFluidStorage)
						{
							TileEntityBusFluidStorage storageBus = (TileEntityBusFluidStorage) entry.getTile();
							if (storageBus.getTank() != null && storageBus.getTank().getTankInfo(storageBus.getFacing().getOpposite())[0].fluid != null)
							{
								FluidStack fluidInTank = storageBus.getTank().getTankInfo(storageBus.getFacing().getOpposite())[0].fluid;
								ArrayList<Fluid> fluidsInNetwork = new ArrayList<Fluid>();
								for (FluidStack fluidStack : fluidStacksInNetwork)
								{
									fluidsInNetwork.add(fluidStack.getFluid());
								}
								if (fluidsInNetwork.contains(fluidInTank.getFluid()))
								{
									fluidStacksInNetwork.set(fluidsInNetwork.indexOf(fluidInTank.getFluid()), new FluidStack(fluidInTank.getFluid(), fluidStacksInNetwork.get(fluidsInNetwork.indexOf(fluidInTank.getFluid())).amount + fluidInTank.amount));
								} else
								{
									fluidStacksInNetwork.add(fluidInTank);
								}
							}
						}
					} catch (AppEngTileMissingException e)
					{
						e.printStackTrace();
					}
				} else
				{
					fluidStacksInNetwork = new ArrayList<FluidStack>();
				}
			}
		} else
		{
			fluidStacksInNetwork = new ArrayList<FluidStack>();
		}
	}

	public void setCurrentFluid(int fluidIndex)
	{
		this.fluidIndex = fluidIndex;
	}

	public int getCurrentFluid()
	{
		return fluidIndex;
	}

	public ArrayList<FluidStack> getFluidsInNetwork()
	{
		return fluidStacksInNetwork;
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
}
