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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidContainerItem;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.config.RedstoneModeInput;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.gui.widget.WidgetFluidModes.FluidMode;
import extracells.util.ECPrivateInventory;

public class TileEntityBusFluidImport extends ColorableECTile implements IGridMachine, IDirectionalMETile, IFluidHandler, ITileCable
{
	Boolean powerStatus = true, redstoneFlag = false, networkReady = true, redstoneStatus;
	IGridInterface grid;
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.import");
	ECPrivateInventory inventory = new ECPrivateInventory(costumName, 8, 1);
	RedstoneModeInput redstoneMode = RedstoneModeInput.Ignore;
	FluidMode fluidMode = FluidMode.DROPS;

	public TileEntityBusFluidImport()
	{
		powerStatus = false;
	}

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote && isPowered())
		{
			switch (getRedstoneMode())
			{
			case WhenOn:
				if (redstoneStatus)
				{
					doWork(fluidMode);
				}
				break;
			case WhenOff:
				if (!redstoneStatus)
				{
					doWork(fluidMode);
				}
				break;
			case OnPulse:
				if (!redstoneStatus)
				{
					redstoneFlag = false;
				} else
				{
					if (!redstoneFlag)
					{
						doWork(fluidMode);
					} else
					{
						redstoneFlag = true;
						doWork(fluidMode);
					}
				}
				break;
			case Ignore:
				doWork(fluidMode);
				break;
			default:
				break;
			}
		}
	}

	public void setRedstoneStatus(boolean redstone)
	{
		redstoneStatus = redstone;
	}

	private void doWork(FluidMode mode)
	{
		ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata());
		TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);

		if (grid != null && facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
		{
			IFluidHandler tank = (IFluidHandler) facingTileEntity;

			FluidStack drainable = tank.drain(facing.getOpposite(), mode.getAmount(), false);

			if (drainable != null && drainable.amount > 0)
			{
				List<Fluid> fluidFilter = getFilterFluids(inventory.slots);
				IAEItemStack toImport = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), drainable.amount, drainable.fluidID));

				IMEInventoryHandler cellArray = getGrid().getCellArray();
				if (cellArray != null)
				{
					IAEItemStack notImported = cellArray.calculateItemAddition(toImport.copy());

					if (fluidFilter != null && !fluidFilter.isEmpty() && fluidFilter.size() > 0)
					{
						if (fluidFilter.contains(drainable.getFluid()))
						{
							if (grid.useMEEnergy(mode.getCost(), "Import Fluid") && notImported == null)
							{
								FluidStack drained = ((IFluidHandler) facingTileEntity).drain(facing.getOpposite(), (int) toImport.getStackSize(), true);
								if (drained != null)
									cellArray.addItems(toImport.copy());
							}
						}
					} else
					{
						if (grid.useMEEnergy(mode.getCost(), "Import Fluid") && notImported == null)
						{
							FluidStack drained = ((IFluidHandler) facingTileEntity).drain(facing.getOpposite(), (int) toImport.getStackSize(), true);
							if (drained != null)
								cellArray.addItems(toImport.copy());
						}
					}
				}
			}
		}
	}

	public List<Fluid> getFilterFluids(List<ItemStack> filterItemStacks)
	{
		List<Fluid> filterFluids = new ArrayList<Fluid>();

		if (filterItemStacks != null)
		{
			for (ItemStack entry : filterItemStacks)
			{
				if (entry != null)
				{
					if (entry.getItem() instanceof IFluidContainerItem)
					{
						FluidStack contained = ((IFluidContainerItem) entry.getItem()).getFluid(entry);
						if (contained != null)
							filterFluids.add(contained.getFluid());
					}
					if (FluidContainerRegistry.isFilledContainer(entry))
					{
						filterFluids.add(FluidContainerRegistry.getFluidForFilledItem(entry).getFluid());
					}
				}
			}
		}
		return filterFluids;
	}

	public RedstoneModeInput getRedstoneMode()
	{
		return redstoneMode;
	}

	public void setRedstoneMode(RedstoneModeInput mode)
	{
		redstoneMode = mode;
	}

	public FluidMode getFluidMode()
	{
		return fluidMode;
	}

	public void setFluidMode(FluidMode mode)
	{
		fluidMode = mode;
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
		nbt.setTag("Items", inventory.writeToNBT());
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}

		nbt.setInteger("RedstoneMode", getRedstoneMode().ordinal());
		nbt.setInteger("FluidMode", getFluidMode().ordinal());
		nbt.setBoolean("RedstoneState", redstoneStatus);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		inventory.readFromNBT(nbttaglist);
		if (nbt.hasKey("CustomName"))
		{
			costumName = nbt.getString("CustomName");
		}

		setRedstoneMode(RedstoneModeInput.values()[nbt.getInteger("RedstoneMode")]);
		setFluidMode(FluidMode.values()[nbt.getInteger("FluidMode")]);
		redstoneStatus = nbt.getBoolean("RedstoneState");
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource != null && getGrid() != null && isPowered() && from.ordinal() == this.blockMetadata)
		{
			IAEItemStack added;
			int amount = resource.amount;
			int fluidID = resource.fluidID;
			IAEItemStack temp = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), amount, fluidID));
			temp.setStackSize(amount);
			IMEInventoryHandler cellArray = getGrid().getCellArray();
			if (cellArray != null)
			{
				if (doFill)
				{
					added = cellArray.addItems(temp);
				} else
				{
					added = cellArray.calculateItemAddition(temp);
				}
				if (added == null)
				{
					if (doFill)
						getGrid().useMEEnergy(amount / 50, "Import Fluid");
					return resource.amount;
				} else
				{
					if (doFill)
						getGrid().useMEEnergy(amount - added.getStackSize() / 50, "Import Fluid");
					return (int) (resource.amount - added.getStackSize());
				}
			}
		}
		return 0;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		return null;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		return null;
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		if (grid != null)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			return cellArray != null && fluid != null && cellArray.canAccept(Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, fluid.getID())));
		}
		return false;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return false;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		if (getGrid() != null && from.ordinal() == this.blockMetadata)
		{
			List<FluidTankInfo> tankInfo = new ArrayList<FluidTankInfo>();
			FluidTankInfo[] tankArray = new FluidTankInfo[1];

			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				for (IAEItemStack item : cellArray.getAvailableItems())
				{
					if (item.getItem() == FLUIDDISPLAY.getItemInstance())
						tankInfo.add(new FluidTankInfo(new FluidStack(FluidRegistry.getFluid(item.getItemDamage()), (int) item.getStackSize()), (int) getGrid().getCellArray().freeBytes()));
				}

				if (tankInfo.isEmpty())
					tankInfo.add(new FluidTankInfo(null, (int) cellArray.freeBytes()));

				tankArray = tankInfo.toArray(tankArray);
				return tankArray;
			}
		}
		return null;
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
