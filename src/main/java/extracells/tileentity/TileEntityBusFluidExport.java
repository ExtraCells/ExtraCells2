package extracells.tileentity;

import static extracells.ItemEnum.FLUIDDISPLAY;

import java.util.ArrayList;
import java.util.List;

import extracells.Extracells;
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
import net.minecraftforge.fluids.FluidStack;
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
import extracells.BlockEnum;
import extracells.gui.widget.WidgetFluidModes.FluidMode;
import extracells.util.ECPrivateInventory;

public class TileEntityBusFluidExport extends ColorableECTile implements IGridMachine, IDirectionalMETile, ITileCable
{
	private boolean powerStatus = true, redstoneFlag = false, networkReady = true, redstoneStatus = false, fluidHandlerCached = false, redStoneCached = false;
	private IGridInterface grid;
	private String customName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	private ECPrivateInventory inventory = new ECPrivateInventory(customName, 8, 1);
	private RedstoneModeInput redstoneMode = RedstoneModeInput.Ignore;
	private FluidMode fluidMode = FluidMode.DROPS;
	private IFluidHandler fluidHandler = null;
	private int currentTick = 0;
	private final int tickRate = Extracells.tickRateExport;

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote)
		{
			currentTick++;
			if (currentTick == tickRate)
			{
				currentTick = 0;
				doUpdateEntity();
			}
		}
	}

	public void doUpdateEntity()
	{
		if (!redStoneCached || !fluidHandlerCached)
		{
			BlockEnum.FLUIDEXPORT.getBlockInstance().onNeighborBlockChange(worldObj, xCoord, yCoord, zCoord, 1);
			fluidHandlerCached = redStoneCached = true;
		}

		if (!worldObj.isRemote && isPowered() && grid != null && fluidHandler != null)
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

	public void setFluidHandler(IFluidHandler handler)
	{
		fluidHandler = handler;
	}

	public void setRedstoneStatus(boolean redstone)
	{
		redstoneStatus = redstone;
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

	private void doWork(FluidMode mode)
	{
		List<Fluid> fluidFilter = getFilterFluids(inventory.slots);

		if (fluidFilter != null && fluidFilter.size() > 0)
		{
			IMEInventoryHandler cellArray = getGrid().getCellArray();
			for (Fluid entry : fluidFilter)
			{
				if (entry != null && cellArray != null)
				{
					IAEItemStack entryToAEIS = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), 1, entry.getID()));

					long contained = cellArray.countOfItemType(entryToAEIS);

					if (contained > 0)
					{
						int modeAmount = mode.getAmount() * tickRate;
						exportFluid(new FluidStack(entry, contained < modeAmount ? (int) contained : modeAmount), fluidHandler, ForgeDirection.getOrientation(getBlockMetadata()).getOpposite(), mode);
					}
				}
			}
		}
	}

	public void exportFluid(FluidStack toExport, IFluidHandler tankToFill, ForgeDirection from, FluidMode mode)
	{
		if (toExport == null)
			return;

		int fillable = tankToFill.fill(from, toExport, false);

		if (fillable > 0)
		{
			int filled = tankToFill.fill(from, toExport, true);

			IAEItemStack toExtract = Util.createItemStack(new ItemStack(FLUIDDISPLAY.getItemInstance(), filled, toExport.fluidID));

			IMEInventoryHandler cellArray = grid.getCellArray();
			if (cellArray != null)
			{
				IAEItemStack extracted = cellArray.extractItems(toExtract);

				grid.useMEEnergy(mode.getCost() * tickRate, "Export Fluid");

				if (extracted == null)
				{
					toExport.amount = filled;
					tankToFill.drain(from, toExport, true);
				} else if (extracted.getStackSize() < filled)
				{
					toExport.amount = (int) (filled - (filled - extracted.getStackSize()));
					tankToFill.drain(from, toExport, true);
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
			nbt.setString("CustomName", this.customName);
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
			customName = nbt.getString("CustomName");
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
