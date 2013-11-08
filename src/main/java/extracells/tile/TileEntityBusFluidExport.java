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
import extracells.ItemEnum;
import extracells.SpecialFluidStack;

public class TileEntityBusFluidExport extends ColorableECTile implements IGridMachine, IDirectionalMETile, ITileCable
{
	Boolean powerStatus = true, redstoneFlag = false, networkReady = true;
	IGridInterface grid;
	ItemStack[] filterSlots = new ItemStack[8];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();
	ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 1);
	RedstoneModeInput redstoneAction = RedstoneModeInput.Ignore;

	@Override
	public void updateEntity()
	{
		if (!worldObj.isRemote && isPowered())
		{
			switch (getRedstoneAction())
			{
			case WhenOn:
				if (worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord))
				{
					doWork();
				}
				break;
			case WhenOff:
				if (!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord))
				{
					doWork();
				}
				break;
			case OnPulse:
				if (!worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || !worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord))
				{
					redstoneFlag = false;
				} else
				{
					if (!redstoneFlag)
					{
						doWork();
					} else
					{
						redstoneFlag = true;
						doWork();
					}
				}
				break;
			case Ignore:
				doWork();
				break;
			default:
				break;
			}
		}
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

	private void doWork()
	{
		ForgeDirection facing = ForgeDirection.getOrientation(getBlockMetadata());
		TileEntity facingTileEntity = worldObj.getBlockTileEntity(xCoord + facing.offsetX, yCoord + facing.offsetY, zCoord + facing.offsetZ);

		if (grid != null && facingTileEntity != null && facingTileEntity instanceof IFluidHandler)
		{
			IFluidHandler facingTank = (IFluidHandler) facingTileEntity;

			List<Fluid> fluidFilter = getFilterFluids(filterSlots);

			if (fluidFilter != null && fluidFilter.size() > 0)
			{
				for (Fluid entry : fluidFilter)
				{
					if (entry != null)
					{// sometimes the grid becomes null after the null check, it happens so rarely, that a trycatch wont be bad for performance.
						try
						{
							IAEItemStack entryToAEIS = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, entry.getID()));

							long contained = getGrid().getCellArray().countOfItemType(entryToAEIS);

							if (contained > 0)
							{
								exportFluid(new FluidStack(entry, contained < 20 ? (int) contained : 20), facingTank, facing.getOpposite());
							}
						} catch (NullPointerException e)
						{
						}
					}
				}
			}
		}
	}

	public void exportFluid(FluidStack toExport, IFluidHandler tankToFill, ForgeDirection from)
	{
		if (toExport == null)
			return;

		int fillable = tankToFill.fill(from, toExport, false);

		if (fillable > 0)
		{
			int filled = tankToFill.fill(from, toExport, true);

			IAEItemStack toExtract = Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), filled, toExport.fluidID));

			IAEItemStack extracted = grid.getCellArray().extractItems(toExtract);

			grid.useMEEnergy(12.0F, "Export Fluid");

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

	public List<Fluid> getFilterFluids(ItemStack[] filterItemStacks)
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

	public RedstoneModeInput getRedstoneAction()
	{
		return redstoneAction;
	}

	public void setRedstoneAction(RedstoneModeInput mode)
	{
		redstoneAction = mode;
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
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}

		nbt.setInteger("RedstoneMode", getRedstoneAction().ordinal());
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

		setRedstoneAction(RedstoneModeInput.values()[nbt.getInteger("RedstoneMode")]);
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
