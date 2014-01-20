package extracells.tileentity;

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
import net.minecraftforge.fluids.*;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.ItemEnum;
import extracells.integration.logisticspipes.IFluidNetworkAccess;
import extracells.util.ECPrivateInventory;
import extracells.util.SpecialFluidStack;

@Optional.Interface(iface = "logisticspipes.api.IRequestAPI", modid = "LogisticsPipes|Main")
public class TileEntityInterfaceFluid extends ColorableECTile implements IGridMachine, IFluidHandler, IStorageAware, IFluidNetworkAccess
{
	private Boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	public FluidTank[] tanks = new FluidTank[6];
	private String customName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	private ECPrivateInventory inventory = new ECPrivateInventory(customName, 6, 1);
	private List<SpecialFluidStack> fluidList = new ArrayList<SpecialFluidStack>();

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
					int filled = (int) fillToNetwork(tank.drain(20, false), true);
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
						int drained = (int) drainFromNetwork(new FluidStack(filterFluid, tank.fill(new FluidStack(filterFluid, 20), false)), true);
						if (drained > 0)
						{
							tank.fill(new FluidStack(filterFluid, drained), true);
							PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
						}
					}
				} else
				{
					int filled = (int) fillToNetwork(tank.drain(20, false), true);
					if (filled > 0)
					{
						tank.drain(filled, true);
						PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
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

	/* IStorageAware; used for Logistics Pipes */
	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		fluidList = new ArrayList<SpecialFluidStack>();
		if (iss != null)
		{
			for (IAEItemStack stack : iss)
			{
				if (stack != null && stack.getItem() == ItemEnum.FLUIDDISPLAY.getItemInstance())
				{
					fluidList.add(new SpecialFluidStack(stack.getItemDamage(), stack.getStackSize()));
				}
			}
		}
	}

	/* IFluidNetworkAccess for Logistics Pipes */
	@Override
	public List<SpecialFluidStack> getFluidsInNetwork()
	{
		if (!isMachineActive() || grid == null || grid.getCellArray() == null)
			return null;
		return fluidList;
	}

	@Override
	public long drainFromNetwork(FluidStack toDrain, boolean doDrain)
	{
		if (!isMachineActive() || toDrain == null || grid == null)
			return 0;
		IMEInventoryHandler cellArray = grid.getCellArray();
		if (cellArray == null)
			return 0;
		IAEItemStack drained = cellArray.extractItems(createFluidItemStack(toDrain));
		if (drained == null)
			return 0;
		if (!doDrain)
			cellArray.addItems(drained);

		getGrid().useMEEnergy(drained.getStackSize() == 0 ? 0 : drained.getStackSize() / 4, "FluidInterface");
		return drained.getStackSize();
	}

	@Override
	public long fillToNetwork(FluidStack toFill, boolean doFill)
	{
		if (!isMachineActive() || toFill == null || grid == null)
			return 0;
		IMEInventoryHandler cellArray = grid.getCellArray();
		if (cellArray == null)
			return 0;
		IAEItemStack notFilled = cellArray.calculateItemAddition(createFluidItemStack(toFill));
		IAEItemStack filled = createFluidItemStack(toFill);
		if (notFilled != null)
			filled = createFluidItemStack(new SpecialFluidStack(notFilled.getItemDamage(), toFill.amount - notFilled.getStackSize()));
		if (doFill)
			cellArray.addItems(filled);

		getGrid().useMEEnergy(filled.getStackSize() == 0 ? 0 : filled.getStackSize() / 4, "FluidInterface");
		return filled.getStackSize();
	}

	@Override
	public TileEntity getNetworkController()
	{
		if (!isMachineActive() || grid == null)
			return null;
		return grid.getController();
	}

	public IAEItemStack createFluidItemStack(SpecialFluidStack stack)
	{
		IAEItemStack toReturn = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), 1, stack.getID()));
		toReturn.setStackSize(stack.getAmount());
		return toReturn;
	}

	public IAEItemStack createFluidItemStack(FluidStack stack)
	{
		return createFluidItemStack(new SpecialFluidStack(stack.fluidID, stack.amount));
	}
}
