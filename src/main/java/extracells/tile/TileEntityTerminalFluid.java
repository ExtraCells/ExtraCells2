package extracells.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
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
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.ItemEnum;
import extracells.SpecialFluidStack;

public class TileEntityTerminalFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile, IStorageAware
{
	Boolean powerStatus = true, networkReady = true;
	IGridInterface grid;
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.terminal");
	private ItemStack[] slots = new ItemStack[3];
	private Fluid currentFluid = null;
	ECPrivateInventory inventory = new ECPrivateInventory(slots, costumName, 64)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return FluidContainerRegistry.isContainer(itemstack) || (itemstack != null && itemstack.getItem() instanceof IFluidContainerItem);
		}
	};
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();

	public void updateEntity()
	{
		if (!worldObj.isRemote && isMachineActive())
		{
			ItemStack input = getInventory().getStackInSlot(0);
			ItemStack output = getInventory().getStackInSlot(1);

			if (!fluidsInNetwork.isEmpty())
			{
				if (currentFluid == null)
					currentFluid = fluidsInNetwork.get(0).getFluid();

				if (currentFluid != null)
				{
					if (input != null)
					{
						if (FluidContainerRegistry.isEmptyContainer(input))
						{
							FluidStack request = new FluidStack(currentFluid, 1000);

							ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(request, input);

							if (filledContainer != null)
							{
								if (output == null)
								{
									if (drainFluid(request))
									{
										getInventory().setInventorySlotContents(1, FluidContainerRegistry.fillFluidContainer(request, input));
										getInventory().decrStackSize(0, 1);
									}
								} else if (output.isStackable() && output.stackSize < output.getMaxStackSize() && output.getItem() == filledContainer.getItem() && output.getItemDamage() == filledContainer.getItemDamage() && output.getTagCompound() == filledContainer.getTagCompound())
								{
									if (drainFluid(request))
									{
										output.stackSize = output.stackSize + 1;
										getInventory().decrStackSize(0, 1);
									}
								}
							}
						} else if (input.getItem() instanceof IFluidContainerItem)
						{
							ItemStack inputTemp = input.copy();
							inputTemp.stackSize = 1;

							IFluidContainerItem fluidContainerItem = (IFluidContainerItem) inputTemp.getItem();

							if (fluidContainerItem.getFluid(inputTemp) == null || fluidContainerItem.getFluid(inputTemp).amount == 0)
							{

								FluidStack request = new FluidStack(currentFluid, fluidContainerItem.getCapacity(inputTemp));

								ItemStack inputToBeFilled = inputTemp.copy();
								inputToBeFilled.stackSize = 1;
								int filledAmount = fluidContainerItem.fill(inputToBeFilled, request, true);

								if (output == null)
								{
									if (drainFluid(request))
									{
										getInventory().setInventorySlotContents(1, inputToBeFilled);
										getInventory().decrStackSize(0, 1);
									}
								} else if (output != null && output.itemID == inputToBeFilled.itemID && (!inputToBeFilled.getHasSubtypes() || inputToBeFilled.getItemDamage() == output.getItemDamage()) && ItemStack.areItemStackTagsEqual(inputToBeFilled, output))
								{
									if (output.stackSize + inputToBeFilled.stackSize <= inputToBeFilled.getMaxStackSize())
									{
										if (drainFluid(request))
										{
											output.stackSize = output.stackSize + 1;
											getInventory().decrStackSize(0, 1);
										}
									}
								}
							}
						}
					}
				}
			} else
			{
				getInventory().setInventorySlotContents(2, null);
			}

			if (FluidContainerRegistry.isFilledContainer(input))
			{
				ItemStack drainedContainer = input.getItem().getContainerItemStack(input);
				FluidStack containedFluid = FluidContainerRegistry.getFluidForFilledItem(input);

				if (FluidContainerRegistry.getFluidForFilledItem(input) != null)
				{
					if (output == null)
					{
						if (fillFluid(FluidContainerRegistry.getFluidForFilledItem(input)))
						{
							getInventory().setInventorySlotContents(1, drainedContainer);
							getInventory().decrStackSize(0, 1);
						}
					} else if (output.isStackable() && output.stackSize < output.getMaxStackSize())
					{
						if (drainedContainer == null)
						{
							if (fillFluid(FluidContainerRegistry.getFluidForFilledItem(input)))
							{

								getInventory().decrStackSize(0, 1);
							}
						} else if (output.isStackable() && output.stackSize < output.getMaxStackSize() && output.getItem() == drainedContainer.getItem() && output.getItemDamage() == drainedContainer.getItemDamage() && output.getTagCompound() == drainedContainer.getTagCompound())
						{
							if (fillFluid(FluidContainerRegistry.getFluidForFilledItem(input)))
							{
								output.stackSize = output.stackSize + 1;
								getInventory().decrStackSize(0, 1);
							}
						}
					}
				}
			} else if (input != null && input.getItem() instanceof IFluidContainerItem)
			{
				ItemStack inputTemp = input.copy();
				inputTemp.stackSize = 1;

				IFluidContainerItem fluidContainerItem = (IFluidContainerItem) inputTemp.getItem();
				FluidStack containedFluid = fluidContainerItem.getFluid(inputTemp);

				if (containedFluid != null && containedFluid.amount > 0)
				{
					ItemStack inputToBeDrained = inputTemp.copy();
					inputToBeDrained.stackSize = 1;

					int drainedAmount = fluidContainerItem.drain(inputToBeDrained, containedFluid.amount, true).amount;

					if (output == null)
					{
						if (fillFluid(containedFluid))
						{
							getInventory().setInventorySlotContents(1, inputToBeDrained);
							getInventory().decrStackSize(0, 1);
						}
					} else if (output.isStackable() && output.stackSize < output.getMaxStackSize())
					{
						if (output != null && output.itemID == inputToBeDrained.itemID && (!inputToBeDrained.getHasSubtypes() || inputToBeDrained.getItemDamage() == output.getItemDamage()) && ItemStack.areItemStackTagsEqual(inputToBeDrained, output))
						{
							if (output.stackSize + inputToBeDrained.stackSize <= inputToBeDrained.getMaxStackSize())
							{
								if (fillFluid(containedFluid))
								{
									output.stackSize = output.stackSize + 1;
									getInventory().decrStackSize(0, 1);
								}
							}
						}
					}
				}
			}
		}
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		updateFluids(iss);
	}

	public void updateFluids(IItemList currentItems)
	{
		fluidsInNetwork = new ArrayList<SpecialFluidStack>();

		if (grid != null)
		{
			for (IAEItemStack itemstack : currentItems)
			{
				if (itemstack.getItem() == ItemEnum.FLUIDDISPLAY.getItemEntry() && itemstack.getStackSize() > 0)
				{
					fluidsInNetwork.add(new SpecialFluidStack(itemstack.getItemDamage(), itemstack.getStackSize()));
				}
			}
		}

		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public List<SpecialFluidStack> getFluids()
	{
		return fluidsInNetwork;
	}

	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);

		NBTTagCompound fluids = new NBTTagCompound();
		int[] fluidAmounts = new int[fluidsInNetwork.size()];
		int[] fluidIDs = new int[fluidsInNetwork.size()];
		for (int i = 0; i < fluidsInNetwork.size(); i++)
		{
			fluidIDs[i] = fluidsInNetwork.get(i).getID();
			fluids.setLong("FluidAmount#" + i, fluidsInNetwork.get(i).getAmount());
		}
		fluids.setIntArray("FluidIDs", fluidIDs);
		nbtTag.setCompoundTag("fluids", fluids);
		nbtTag.setInteger("currentFluid", currentFluid != null ? currentFluid.getID() : -1);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);

		NBTTagCompound fluids = packet.data.getCompoundTag("fluids");

		fluidsInNetwork = new ArrayList<SpecialFluidStack>();
		int[] fluidIDs = fluids.getIntArray("FluidIDs");
		for (int i = 0; i < fluidIDs.length; i++)
		{
			fluidsInNetwork.add(new SpecialFluidStack(fluidIDs[i], fluids.getLong("FluidAmount#" + i)));
		}
		currentFluid = FluidRegistry.getFluid(packet.data.getInteger("currentFluid"));
	}

	private boolean fillFluid(FluidStack toImport)
	{
		IAEItemStack toFill = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 0, toImport.fluidID));
		toFill.setStackSize(toImport.amount);
		if (grid != null)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				IAEItemStack sim = cellArray.calculateItemAddition(toFill.copy());

				if (sim != null)
				{
					return false;
				}

				cellArray.addItems(toFill.copy());
				return true;
			}
		}
		return false;
	}

	private boolean drainFluid(FluidStack toExport)
	{
		IAEItemStack toDrain = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 0, toExport.fluidID));
		toDrain.setStackSize(toExport.amount);

		if (grid != null)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				for (SpecialFluidStack fluidstack : fluidsInNetwork)
				{
					if (fluidstack.getFluid() == toExport.getFluid() && fluidstack.amount >= toExport.amount)
					{
						IAEItemStack takenStack = cellArray.extractItems(Util.createItemStack(new ItemStack(toDrain.getItem(), (int) (toDrain.getStackSize()), toDrain.getItemDamage())));

						if (takenStack == null)
						{
							return false;
						} else if (takenStack.getStackSize() != (int) toDrain.getStackSize())
						{
							cellArray.addItems(takenStack);
							return false;
						} else
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public void setCurrentFluid(int fluidID)
	{
		this.currentFluid = FluidRegistry.getFluid(fluidID);
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public Fluid getCurrentFluid()
	{
		return currentFluid;
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
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		this.slots = new ItemStack[getInventory().getSizeInventory()];
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
		if (!worldObj.isRemote)
		{
			grid = gi;
			if (gi != null)
			{
				IMEInventoryHandler cellArray = gi.getCellArray();
				if (cellArray != null)
					updateFluids(cellArray.getAvailableItems());
			}
		}
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

	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;

		if (getGrid() != null)
		{
			IMEInventoryHandler cellArray = getGrid().getCellArray();
			if (cellArray != null)
				updateFluids(cellArray.getAvailableItems());
		}
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}
}
