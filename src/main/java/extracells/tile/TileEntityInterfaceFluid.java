package extracells.tile;

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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.ItemEnum;
import extracells.util.ECPrivateInventory;

public class TileEntityInterfaceFluid extends ColorableECTile implements IGridMachine, IFluidHandler
{
	private Boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	public FluidTank[] tanks = new FluidTank[6];
	private String customName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	private ECPrivateInventory inventory = new ECPrivateInventory(customName, 6, 1);

	public TileEntityInterfaceFluid()
	{
		for (int i = 0; i < tanks.length; i++)
		{
			tanks[i] = new FluidTank(10000)
			{
				public FluidTank readFromNBT(NBTTagCompound nbt)
				{
					if (!nbt.hasKey("Empty"))
					{
						FluidStack fluid = FluidStack.loadFluidStackFromNBT(nbt);
						setFluid(fluid);
					} else
					{
						setFluid(null);
					}
					return this;
				}
			};
		}
	}

	public void updateEntity()
	{
		for (int i = 0; i < tanks.length; i++)
		{
			FluidTank tank = tanks[i];
			FluidStack tankFluid = tanks[i].getFluid();
			Fluid filterFluid = inventory.slots.get(i) != null ? FluidRegistry.getFluid(inventory.slots.get(i).getItemDamage()) : null;
			if (filterFluid == null)
			{
				if (tankFluid != null)
				{
					int filled = fillToNetwork(tank.drain(20, false), true);
					if (filled > 0)
					{
						tank.drain(filled, true);
						PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
					}
				}
			} else
			{
				if (tank.getFluid() == null || tank.getFluid().getFluid() == filterFluid)
				{
					if (tank.getFluid() == null || tank.getFluid().amount < 10000)
					{
						FluidStack drained = drainFromNetwork(new FluidStack(filterFluid, tank.fill(new FluidStack(filterFluid, 20), false)));
						if (drained != null)
						{
							tank.fill(drained, true);
							PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
						}
					}
				} else
				{
					int filled = fillToNetwork(tank.drain(20, false), true);
					if (filled > 0)
					{
						tank.drain(filled, true);
						PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
					}
				}
			}
		}
	}

	public FluidStack drainFromNetwork(FluidStack toDrain)
	{
		if (getGrid() == null || getGrid().getCellArray() == null || toDrain == null)
			return null;
		IAEItemStack request = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), 1, toDrain.fluidID));
		request.setStackSize(toDrain.amount);
		IAEItemStack extracted = getGrid().getCellArray().extractItems(request);
		if (extracted == null)
			return null;

		getGrid().useMEEnergy(extracted.getStackSize() / 4, "FluidInterface");
		return new FluidStack(extracted.getItemDamage(), (int) extracted.getStackSize());
	}

	public int fillToNetwork(FluidStack toFill, Boolean doFill)
	{
		if (getGrid() == null || getGrid().getCellArray() == null || toFill == null)
			return 0;
		IAEItemStack request = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), 1, toFill.fluidID));
		request.setStackSize(toFill.amount);
		IAEItemStack added = null;
		if (doFill)
		{
			added = getGrid().getCellArray().addItems(request);
		} else
		{
			added = getGrid().getCellArray().calculateItemAddition(request);
		}
		if (added == null)
			return toFill.amount;

		getGrid().useMEEnergy(added.getStackSize() / 4, "FluidInterface");
		return toFill.amount - (int) added.getStackSize();
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
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
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		inventory.readFromNBT(nbttaglist);
		if (nbt.hasKey("CustomName"))
		{
			this.customName = nbt.getString("CustomName");
		}

		for (int i = 0; i < tanks.length; i++)
		{
			NBTTagCompound tankNBT = nbt.getCompoundTag("tank#" + i);
			tanks[i].readFromNBT(tankNBT);
		}
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
		for (int i = 0; i < tanks.length; i++)
		{
			NBTTagCompound tankNBT = new NBTTagCompound();
			tanks[i].writeToNBT(tankNBT);
			nbt.setCompoundTag("tank#" + i, tankNBT);
		}
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

	/* IGridMachine */
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
		return 5.0F;
	}

	@Override
	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;
	}

	@Override
	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (from == ForgeDirection.UNKNOWN || resource == null)
			return 0;

		int filled = 0;
		filled += fillToNetwork(resource, doFill);

		if (filled < resource.amount)
			filled += tanks[from.ordinal()].fill(new FluidStack(resource.fluidID, resource.amount - filled), doFill);
		if (filled > 0)
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		FluidStack tankFluid = tanks[from.ordinal()].getFluid();
		if (resource == null || tankFluid == null || tankFluid.getFluid() != resource.getFluid())
			return null;
		return drain(from, resource.amount, doDrain);

	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (from == ForgeDirection.UNKNOWN)
			return null;
		FluidStack drained = tanks[from.ordinal()].drain(maxDrain, doDrain);
		if (drained != null)
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		return drained;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if (from == ForgeDirection.UNKNOWN)
			return false;
		return tanks[from.ordinal()].fill(new FluidStack(fluid, 1), false) > 0;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		if (from == ForgeDirection.UNKNOWN)
			return false;
		FluidStack tankFluid = tanks[from.ordinal()].getFluid();
		return tankFluid != null ? tankFluid.getFluid() == fluid : null;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if (from == ForgeDirection.UNKNOWN)
			return null;
		return new FluidTankInfo[]
		{ tanks[from.ordinal()].getInfo() };
	}
}
