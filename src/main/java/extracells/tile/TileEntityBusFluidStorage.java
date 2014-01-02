package extracells.tile;

import static extracells.ItemEnum.FLUIDDISPLAY;

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
import extracells.handler.FluidBusInventoryHandler;
import extracells.util.ECPrivateInventory;

public class TileEntityBusFluidStorage extends ColorableECTile implements IGridMachine, IDirectionalMETile, ICellContainer, ITileCable
{
	Boolean powerStatus = true, networkReady = true;
	IGridInterface grid;
	int priority = 1;
	private String customName = StatCollector.translateToLocal("tile.block.fluid.bus.storage");
	ECPrivateInventory inventory = new ECPrivateInventory(customName, 54, 1);
	FluidStack lastFluid;

	public void setPriority(int priority)
	{
		this.priority = priority;
	}

	@Override
	public void updateEntity()
	{
		if (getGrid() == null || worldObj.isRemote)
			return;

		TileEntity tankTE = worldObj.getBlockTileEntity(xCoord + getFacing().offsetX, yCoord + getFacing().offsetY, zCoord + getFacing().offsetZ);
		FluidStack tankFluid = null;
		if (tankTE instanceof IFluidHandler)
		{
			IFluidHandler tank = (IFluidHandler) tankTE;
			if (tank != null)
			{
				FluidTankInfo[] tankInfos = tank.getTankInfo(getFacing().getOpposite());

				if (tankInfos != null && tankInfos.length > 0)
				{
					if (tankInfos[0] != null)
						tankFluid = tankInfos[0].fluid;
				}
			}
		}

		if (tankFluid != null && !tankFluid.isFluidStackIdentical(lastFluid) || lastFluid != null && !lastFluid.isFluidStackIdentical(tankFluid))
		{
			if (lastFluid != null)
			{
				IAEItemStack toRemove = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, lastFluid.fluidID));
				toRemove.setStackSize(lastFluid.amount);
				getGrid().notifyExtractItems(toRemove);
			}

			if (tankFluid != null)
			{
				IAEItemStack toAdd = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, tankFluid.fluidID));
				toAdd.setStackSize(tankFluid.amount);
				getGrid().notifyAddItems(toAdd);

				lastFluid = tankFluid.copy();
			} else
			{
				lastFluid = null;
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
		nbt.setTag("Items", inventory.writeToNBT());
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.customName);
		}
		nbt.setInteger("Priority", getPriority());
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

		if (inventory.slots.size() > 0)
			for (ItemStack itemStack : inventory.slots)
			{
				ItemStack fluidContainer;

				if (FluidContainerRegistry.isFilledContainer(itemStack))
				{
					fluidContainer = new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, FluidContainerRegistry.getFluidForFilledItem(itemStack).getFluid().getID());
					filter.add(fluidContainer);
				} else if (itemStack != null && itemStack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack) != null)
				{
					fluidContainer = new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, ((IFluidContainerItem) itemStack.getItem()).getFluid(itemStack).fluidID);
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