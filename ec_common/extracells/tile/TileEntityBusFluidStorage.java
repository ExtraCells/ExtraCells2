package extracells.tile;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.ICellContainer;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.handler.FluidBusInventoryHandler;

public class TileEntityBusFluidStorage extends TileEntity implements IGridMachine, IDirectionalMETile, ICellContainer, IInventory
{
	Boolean powerStatus;
	IGridInterface grid = null;
	int priority = 1;
	ItemStack[] filterSlots = new ItemStack[54];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.storage");

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public boolean canUpdate()
	{
		return false;
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

	public ForgeDirection getFacing()
	{
		return ForgeDirection.getOrientation(getBlockMetadata());
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
		this.filterSlots = new ItemStack[this.getSizeInventory()];
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
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void setPowerStatus(boolean hasPower)
	{
		System.out.println(hasPower ? "hasPower" : "hasNoPower");
		powerStatus = hasPower;
	}

	@Override
	public boolean isPowered()
	{
		System.out.println(powerStatus ? "isPowered" : "isNotPowered");
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
		return 0;
	}

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(xCoord, yCoord, zCoord);
	}

	@Override
	public List<IMEInventoryHandler> getCellArray()
	{
		List<ItemStack> filter = new ArrayList<ItemStack>();

		if (filterSlots.length != 0)
			for (ItemStack itemStack : filterSlots)
			{
				if (FluidContainerRegistry.isFilledContainer(itemStack))
				{
					ItemStack fluidContainer = new ItemStack(extracells.Extracells.FluidDisplay, 1, FluidContainerRegistry.getFluidForFilledItem(itemStack).getFluid().getID());
					filter.add(fluidContainer);
				}
			}

		List<IMEInventoryHandler> list = new ArrayList<IMEInventoryHandler>();
		list.add(new FluidBusInventoryHandler(worldObj.getBlockTileEntity(xCoord + getFacing().offsetX, yCoord + getFacing().offsetY, zCoord + getFacing().offsetZ), getFacing(), getPriority(), filter));
		return list;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	@Override
	public int getSizeInventory()
	{
		return filterSlots.length;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return filterSlots[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.filterSlots[i] != null)
		{
			ItemStack itemstack;
			if (this.filterSlots[i].stackSize <= j)
			{
				itemstack = this.filterSlots[i];
				this.filterSlots[i] = null;
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				itemstack = this.filterSlots[i].splitStack(j);
				if (this.filterSlots[i].stackSize == 0)
				{
					this.filterSlots[i] = null;
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
		if (this.filterSlots[i] != null)
		{
			ItemStack itemstack = this.filterSlots[i];
			this.filterSlots[i] = null;
			return itemstack;
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.filterSlots[i] = itemstack;

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