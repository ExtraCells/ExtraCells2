package extracells.tile;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridStorageUpdateEvent;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.ICellContainer;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.ItemEnum;
import extracells.handler.FluidBusInventoryHandler;

public class TileEntityBusFluidStorage extends ColorableECTile implements IGridMachine, IDirectionalMETile, ICellContainer, ITileCable
{
	Boolean powerStatus = true, networkReady = true;
	IGridInterface grid;
	int priority = 1;
	ItemStack[] filterSlots = new ItemStack[54];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.storage");
	ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 1);
	FluidStack lastFluid;

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public void updateEntity()
	{
		TileEntity tankTE = worldObj.getBlockTileEntity(xCoord + getFacing().offsetX, yCoord + getFacing().offsetY, zCoord + getFacing().offsetZ);

		if (getGrid() != null && !worldObj.isRemote)
		{
			FluidStack tankFluid = null;
			if (tankTE instanceof IFluidHandler)
			{
				IFluidHandler tank = (IFluidHandler) tankTE;
				if (tank != null)
				{
					FluidTankInfo[] tankInfos = tank.getTankInfo(getFacing().getOpposite());

					if (tankInfos != null)
					{
						if (tankInfos[0] != null)
							tankFluid = tankInfos[0].fluid;
					}
				}
			}

			if (tankFluid != lastFluid)
			{
				if (lastFluid != null)
				{
					IAEItemStack toRemove = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, lastFluid.fluidID));
					toRemove.setStackSize(lastFluid.amount);
					getGrid().notifyExtractItems(toRemove);
				}

				if (tankFluid != null)
				{
					IAEItemStack toAdd = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, tankFluid.fluidID));
					toAdd.setStackSize(tankFluid.amount);
					getGrid().notifyAddItems(toAdd);

					lastFluid = tankFluid.copy();
				} else
				{
					lastFluid = null;
				}
			}
		}
	}

	public void updateGrid()
	{
		if (getGrid() != null)
			MinecraftForge.EVENT_BUS.post(new GridStorageUpdateEvent(worldObj, getLocation(), getGrid()));
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = getColorDataForPacket();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		super.onDataPacket(net, packet);
		readFromNBT(packet.data);
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
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}
		nbt.setInteger("Priority", getPriority());
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
		inventory = new ECPrivateInventory(filterSlots, costumName, 1);
		setPriority(nbt.getInteger("Priority"));
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
				ItemStack fluidContainer;

				if (FluidContainerRegistry.isFilledContainer(itemStack))
				{
					fluidContainer = new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, FluidContainerRegistry.getFluidForFilledItem(itemStack).getFluid().getID());
					filter.add(fluidContainer);
				} else if (itemStack != null && itemStack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack) != null)
				{
					fluidContainer = new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack).fluidID);
					filter.add(fluidContainer);
				}
			}

		List<IMEInventoryHandler> tankHandler = new ArrayList<IMEInventoryHandler>();
		tankHandler.add(new FluidBusInventoryHandler(worldObj.getBlockTileEntity(xCoord + getFacing().offsetX, yCoord + getFacing().offsetY, zCoord + getFacing().offsetZ), getFacing().getOpposite(), getPriority(), filter));

		return tankHandler;
	}

	@Override
	public int getPriority()
	{
		return priority;
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	@Override
	public boolean coveredConnections()
	{
		return false;
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