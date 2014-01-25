package extracells.tileentity;

import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridPatternUpdateEvent;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.exceptions.AppEngTileMissingException;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.util.ICraftingPattern;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.ItemEnum;
import extracells.util.ECPrivateInventory;
import extracells.util.SpecialFluidStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
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
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.*;

import java.util.ArrayList;
import java.util.List;

import static extracells.ItemEnum.FLUIDDISPLAY;

@SuppressWarnings("deprecation")
public class TileEntityTerminalFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile, IStorageAware, ISidedInventory
{
	Boolean powerStatus = false, networkReady = true;
	IGridInterface grid;
	private String customName = StatCollector.translateToLocal("tile.block.fluid.terminal");
	private Fluid currentFluid = null;
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();
	ArrayList<Fluid> craftableFluidsInNetwork = new ArrayList<Fluid>();
	ECPrivateInventory inventory = new ECPrivateInventory(customName, 2, 64)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return FluidContainerRegistry.isContainer(itemstack) || (itemstack != null && itemstack.getItem() instanceof IFluidContainerItem);
		}
	};

	public TileEntityTerminalFluid()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	public void updateEntity()
	{
		if (!worldObj.isRemote && isMachineActive())
		{
			ItemStack input = getInventory().getStackInSlot(0);
			ItemStack output = getInventory().getStackInSlot(1);

			if (!fluidsInNetwork.isEmpty())
			{
				if (currentFluid == null)
					currentFluid = FluidRegistry.getFluid(fluidsInNetwork.get(0).getID());

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
								int filled = fluidContainerItem.fill(inputToBeFilled, request, true);
								inputToBeFilled.stackSize = 1;
								if (filled >= request.amount)
								{
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
				}
			}

			if (FluidContainerRegistry.isFilledContainer(input))
			{
				ItemStack drainedContainer = input.getItem().getContainerItemStack(input);

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
					FluidStack drained = fluidContainerItem.drain(inputToBeDrained, containedFluid.amount, true);
					inputToBeDrained.stackSize = 1;
					if (drained != null && drained.amount >= containedFluid.amount)
					{
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
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	@ForgeSubscribe
	public void onNetworkPatternChange(GridPatternUpdateEvent e)
	{
		if (grid != null)
		{
			IMEInventoryHandler inventoryHandler = grid.getCraftableArray();
			if (inventoryHandler != null)
			{
				craftableFluidsInNetwork = new ArrayList<Fluid>();
				for (IAEItemStack stack : inventoryHandler.getAvailableItems())
				{
					if (stack.getItem() == FLUIDDISPLAY.getItemInstance())
					{
						craftableFluidsInNetwork.add(FluidRegistry.getFluid(stack.getItemDamage()));
					}
				}
			}
		}
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
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
				if (itemstack.getItem() == FLUIDDISPLAY.getItemInstance() && itemstack.getStackSize() > 0)
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

	public List<Fluid> getCurrentCraftables()
	{
		return craftableFluidsInNetwork;
	}

	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = getColorDataForPacket();
		writeToNBT(nbtTag);

		NBTTagCompound fluids = new NBTTagCompound();
		int[] fluidIDs = new int[fluidsInNetwork.size()];
		for (int i = 0; i < fluidsInNetwork.size(); i++)
		{
			fluidIDs[i] = fluidsInNetwork.get(i).getID();
			fluids.setLong("FluidAmount#" + i, fluidsInNetwork.get(i).getAmount());
		}
		fluids.setIntArray("FluidIDs", fluidIDs);
		nbtTag.setCompoundTag("fluids", fluids);

		NBTTagCompound craftableFluids = new NBTTagCompound();
		int[] craftableFluidIDs = new int[craftableFluidsInNetwork.size()];
		for (int i = 0; i < craftableFluidsInNetwork.size(); i++)
		{
			craftableFluidIDs[i] = craftableFluidsInNetwork.get(i).getID();
		}
		craftableFluids.setIntArray("FluidIDs", craftableFluidIDs);
		nbtTag.setCompoundTag("craftablefluids", craftableFluids);

		nbtTag.setInteger("currentFluid", currentFluid != null ? currentFluid.getID() : -1);
		nbtTag.setBoolean("powered", isPowered());
		nbtTag.setBoolean("ready", networkReady);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		super.onDataPacket(net, packet);
		readFromNBT(packet.data);

		NBTTagCompound fluids = packet.data.getCompoundTag("fluids");
		fluidsInNetwork = new ArrayList<SpecialFluidStack>();
		int[] fluidIDs = fluids.getIntArray("FluidIDs");
		for (int i = 0; i < fluidIDs.length; i++)
		{
			fluidsInNetwork.add(new SpecialFluidStack(fluidIDs[i], fluids.getLong("FluidAmount#" + i)));
		}

		NBTTagCompound craftableFluids = packet.data.getCompoundTag("craftablefluids");
		craftableFluidsInNetwork = new ArrayList<Fluid>();
		int[] craftableFluidIDs = craftableFluids.getIntArray("FluidIDs");
		for (int i = 0; i < craftableFluidIDs.length; i++)
		{
			craftableFluidsInNetwork.add(FluidRegistry.getFluid(craftableFluidIDs[i]));
		}
		currentFluid = FluidRegistry.getFluid(packet.data.getInteger("currentFluid"));
		powerStatus = packet.data.getBoolean("powered");
		networkReady = packet.data.getBoolean("ready");
		worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
	}

	private boolean fillFluid(FluidStack toImport)
	{
		IAEItemStack toFill = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 0, toImport.fluidID));
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
		IAEItemStack toDrain = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 0, toExport.fluidID));
		toDrain.setStackSize(toExport.amount);

		if (grid != null)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				for (SpecialFluidStack fluidstack : fluidsInNetwork)
				{
					if (fluidstack.getID() == toExport.fluidID && fluidstack.amount >= toExport.amount)
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

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < inventory.slots.size(); ++i)
		{
			if (inventory.slots.get(i) != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				inventory.slots.get(i).writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbt.setTag("Items", nbttaglist);
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.customName);
		}
		nbt.setInteger("currentFluid", currentFluid != null ? currentFluid.getID() : -1);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		inventory.readFromNBT(nbttaglist);
		if (nbt.hasKey("CustomName"))
		{
			this.customName = nbt.getString("CustomName");
		}
		currentFluid = FluidRegistry.getFluid(nbt.getInteger("currentFluid"));
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
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
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
			} else
			{
				setPowerStatus(false);
			}
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
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

		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}

	@Override
	public int getSizeInventory()
	{
		return inventory.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return inventory.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		return inventory.decrStackSize(i, j);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		return inventory.getStackInSlotOnClosing(i);
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		inventory.setInventorySlotContents(i, itemstack);
	}

	@Override
	public String getInvName()
	{
		return inventory.getInvName();
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return inventory.isInvNameLocalized();
	}

	@Override
	public int getInventoryStackLimit()
	{
		return inventory.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return inventory.isUseableByPlayer(entityplayer);
	}

	@Override
	public void openChest()
	{
	}

	@Override
	public void closeChest()
	{
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return inventory.isItemValidForSlot(i, itemstack);
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int var1)
	{
		return new int[]
		{ 0, 1 };
	}

	@Override
	public boolean canInsertItem(int i, ItemStack itemstack, int j)
	{
		return inventory.isItemValidForSlot(i, itemstack) && i == 0;
	}

	@Override
	public boolean canExtractItem(int i, ItemStack itemstack, int j)
	{
		return i == 1;
	}

	public void requestFluid(FluidStack toOrder)
	{
		ItemStack order = new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), 1, toOrder.fluidID);
		ICraftingPattern pattern = grid.getPatternFor(order);
		System.out.println(pattern == null);
		try
		{
			grid.craftingRequest(order);/*
										 * / ITileCraftingProvider provider = pattern.getProviders().get(0); if (provider instanceof TileEntityInterfaceFluid) ((TileEntityInterfaceFluid) provider).orderFluid(toOrder);//
										 */
		} catch (AppEngTileMissingException wontHappen)
		{
		}
	}
}
