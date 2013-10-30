package extracells.tile;

import net.minecraft.block.Block;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.config.RedstoneModeInput;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;

public class TileEntityBusSupplier extends ColorableECTile implements IGridMachine, IDirectionalMETile, ITileCable
{
	Boolean powerStatus = true, networkReady = true;
	IGridInterface grid;
	ItemStack[] filterSlots = new ItemStack[8];
	private String costumName = StatCollector.translateToLocal("tile.block.bus.supplier");
	ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 64)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			return true;
		}
	};

	public void updateEntity()
	{
		if (getGrid() != null && isMachineActive())
		{
			ForgeDirection facing = ForgeDirection.getOrientation(blockMetadata);
			TileEntity facingTE = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);

			if (facingTE instanceof IInventory)
			{
				IInventory facingInventory = (IInventory) facingTE;
				int[] validSlots = null;

				if (facingTE instanceof ISidedInventory)
				{
					validSlots = ((ISidedInventory) facingTE).getAccessibleSlotsFromSide(facing.getOpposite().ordinal());
				} else
				{
					if (((IInventory) facingTE).getSizeInventory() > 0)
					{
						validSlots = new int[((IInventory) facingTE).getSizeInventory()];

						for (int i = 0; i < ((IInventory) facingTE).getSizeInventory(); i++)
						{
							validSlots[i] = i;
						}
					}
				}

				if (validSlots != null && validSlots.length > 0)
				{
					for (ItemStack currentSlot : filterSlots)
					{
						if (currentSlot != null)
						{
							ItemStack tempCopy = currentSlot.copy();
							int containedInInventory = getCountOfItemInInventory((IInventory) facingTE, tempCopy, validSlots);
							if (containedInInventory < tempCopy.stackSize)
							{
								tempCopy.stackSize -= containedInInventory;

								for (int slotID : validSlots)
								{
									int fillable = fillItems((IInventory) facingTE, slotID, tempCopy.copy(), false);

									long countInNetwork = getGrid().getCellArray().countOfItemType(Util.createItemStack(tempCopy));

									if (countInNetwork < fillable)
										fillable = (int) countInNetwork;

									if (fillable > 0)
									{
										ItemStack toExtract = tempCopy.copy();
										toExtract.stackSize = fillable;
										fillItems((IInventory) facingTE, slotID, toExtract.copy(), true);

										getGrid().getCellArray().extractItems(Util.createItemStack(toExtract.copy()));

										tempCopy.stackSize -= fillable;
										if (tempCopy == null || tempCopy.stackSize <= 0)
											break;
									}
								}
							}
						}
					}
				}
				facingTE.onInventoryChanged();
			}
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.filterSlots.length; ++i)
		{
			if (this.filterSlots[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.filterSlots[i].writeToNBT(nbttagcompound1);
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
		this.filterSlots = new ItemStack[getInventory().getSizeInventory()];
		if (nbt.hasKey("CustomName"))
		{
			this.costumName = nbt.getString("CustomName");
		}
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < this.filterSlots.length)
			{
				this.filterSlots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
		inventory = new ECPrivateInventory(filterSlots, costumName, 1)
		{
			public boolean isItemValidForSlot(int i, ItemStack itemstack)
			{
				return true;
			}
		};
	}

	@Override
	public void validate()
	{
		super.validate();
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	@Override
	public void invalidate()
	{
		super.invalidate();
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent(this, worldObj, getLocation()));
	}

	public int getCountOfItemInInventory(IInventory target, ItemStack toScan, int[] validSlots)
	{
		int contained = 0;
		for (int currentSlotID : validSlots)
		{
			ItemStack currentSlotStack = target.getStackInSlot(currentSlotID);
			if (currentSlotStack != null && currentSlotStack.isItemEqual(toScan))
				contained += currentSlotStack.stackSize;
		}
		return contained;
	}

	public int fillItems(IInventory target, int slotID, ItemStack toFill, boolean doFill)
	{
		ItemStack slotStack = target.getStackInSlot(slotID);

		if (target instanceof ISidedInventory)
		{
			ISidedInventory targetInv = (ISidedInventory) target;

			if (targetInv.canInsertItem(slotID, new ItemStack(toFill.getItem(), 1, toFill.getItemDamage()), blockMetadata))
			{
				if (slotStack == null)
				{
					if (doFill)
						targetInv.setInventorySlotContents(slotID, toFill);
					return toFill.stackSize;
				} else if (slotStack.isItemEqual(toFill) && slotStack.stackSize < 64 && slotStack.stackSize + toFill.stackSize <= 64)
				{
					int fillable = 64 - slotStack.stackSize;

					if (toFill.stackSize > fillable)
						toFill.stackSize = fillable;
					int toReturn = toFill.stackSize;
					toFill.stackSize = target.getStackInSlot(slotID).stackSize + toFill.stackSize;

					if (toFill.stackSize > 0 && doFill)
						target.setInventorySlotContents(slotID, toFill);
					return toReturn;
				}
			}
		} else
		{
			if (slotStack == null)
			{
				if (doFill)
					target.setInventorySlotContents(slotID, toFill);
				return toFill.stackSize;
			} else if (slotStack.isItemEqual(toFill) && slotStack.stackSize < 64 && slotStack.stackSize + toFill.stackSize <= 64)
			{
				int fillable = 64 - slotStack.stackSize;

				if (toFill.stackSize > fillable)
					toFill.stackSize = fillable;
				int toReturn = toFill.stackSize;
				toFill.stackSize = target.getStackInSlot(slotID).stackSize + toFill.stackSize;

				if (toFill.stackSize > 0 && doFill)
				{
					target.setInventorySlotContents(slotID, toFill);
				}
				return toReturn;
			}
		}
		return 0;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);
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
	public boolean coveredConnections()
	{
		return false;
	}

	@Override
	public boolean canConnect(ForgeDirection dir)
	{
		return dir.getOpposite().ordinal() == getBlockMetadata();
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 1.0F;
	}

	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}
}
