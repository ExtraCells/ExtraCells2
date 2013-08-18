package extracells.tile;

import java.util.ArrayList;

import extracells.tile.TileEntityTerminalFluid.SpecialFluidStack;
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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
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

public class TileEntityBusFluidExport extends TileEntity implements IGridMachine, IDirectionalMETile, IInventory, IStorageAware
{
	Boolean powerStatus = false;
	IGridInterface grid;
	ItemStack[] filterSlots = new ItemStack[8];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();

	public TileEntityBusFluidExport()
	{
		updateFluids();
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote && isPowered())
		{
			ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata());
			TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);

			if (grid != null && facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
			{
				IFluidHandler tank = (IFluidHandler) facingTileEntity;
				FluidStack fluidStack = tank.getTankInfo(facing)[0].fluid;
				IAEItemStack toExport = null;

				updateFluids();

				if (fluidStack == null)
				{
					outerloop: for (SpecialFluidStack fluidstack : fluidsInNetwork)
					{
						for (ItemStack itemstack : filterSlots)
						{
							if (itemstack != null && fluidstack.getFluid() == FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid() && fluidstack.amount >= 1000)
							{
								int fluidID = FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid().getID();
								ItemStack temp = new ItemStack(extracells.Extracells.FluidDisplay, 1000, fluidID);
								toExport = Util.createItemStack(temp);
								break outerloop;
							}
						}
					}
				} else
				{
					outerloop: for (SpecialFluidStack fluidstack : fluidsInNetwork)
					{
						for (ItemStack itemstack : filterSlots)
						{
							if (itemstack != null && fluidstack.getFluid() == fluidStack.getFluid() && fluidstack.getFluid() == FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid() && fluidstack.amount >= 1000)
							{
								toExport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 1000, fluidStack.getFluid().getID()));
								break outerloop;
							}
						}
					}
				}

				if (toExport != null && tank.fill(facing, new FluidStack(FluidRegistry.getFluid(toExport.getItemDamage()), 1000), false) == 1000)
				{
					IAEItemStack exportSplit = toExport.copy();
					exportSplit.setStackSize(10);

					if (grid.useMEEnergy(12.0F, "Export Fluid"))
						for (int i = 0; i < 100; i++)
						{
							tank.fill(facing, new FluidStack(FluidRegistry.getFluid(toExport.getItemDamage()), 10), true);
							grid.getCellArray().extractItems(exportSplit);
						}
				}
			}
		}
	}

	private Boolean arrayContains(ItemStack[] array, ItemStack itemstack)
	{
		for (ItemStack entry : array)
		{
			if (entry != null && entry.getItem() == itemstack.getItem() && entry.getItemDamage() == itemstack.getItemDamage())
				return true;
		}
		return false;
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
		return 0;
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

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		updateFluids();
	}
}
