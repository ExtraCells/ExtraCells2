package extracells.tile;

import java.util.ArrayList;

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
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
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
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import extracells.SpecialFluidStack;

public class TileEntityBusFluidExport extends TileEntity implements IGridMachine, IDirectionalMETile, IStorageAware, ITileCable
{
	Boolean powerStatus = false;
	IGridInterface grid;
	ItemStack[] filterSlots = new ItemStack[8];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.export");
	ArrayList<SpecialFluidStack> fluidsInNetwork = new ArrayList<SpecialFluidStack>();
	ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 1);
	Boolean redstoneAction = false;

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

				FluidStack fluidStack;

				if (((IFluidHandler) tank).getTankInfo(facing) != null && tank.getTankInfo(facing).length != 0)
				{
					fluidStack = tank.getTankInfo(facing)[0].fluid;
				} else if (((IFluidHandler) tank).getTankInfo(facing) != null && tank.getTankInfo(ForgeDirection.UNKNOWN).length != 0)
				{
					fluidStack = tank.getTankInfo(ForgeDirection.UNKNOWN)[0].fluid;
				} else
				{
					fluidStack = null;
				}
				IAEItemStack toExport = null;

				updateFluids();

				if (fluidStack == null)
				{
					outerloop: for (SpecialFluidStack fluidstack : fluidsInNetwork)
					{
						for (ItemStack itemstack : filterSlots)
						{
							if (itemstack != null && fluidstack.amount >= 20)
							{
								if (FluidContainerRegistry.getFluidForFilledItem(itemstack) != null && fluidstack.getFluid() == FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid())
								{
									int fluidID = FluidContainerRegistry.getFluidForFilledItem(itemstack).fluidID;
									toExport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluidID));
									break outerloop;
								}
								if (itemstack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack) != null && fluidstack.getFluid() == ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack).getFluid())
								{
									int fluidID = ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack).fluidID;
									toExport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluidID));
									break outerloop;
								}
							}
						}
					}
				} else
				{
					outerloop: for (SpecialFluidStack fluidstack : fluidsInNetwork)
					{
						for (ItemStack itemstack : filterSlots)
						{
							if (itemstack != null && fluidstack.getFluid() == fluidStack.getFluid() && fluidstack.amount >= 20)
							{
								if (FluidContainerRegistry.getFluidForFilledItem(itemstack) != null && fluidstack.getFluid() == FluidContainerRegistry.getFluidForFilledItem(itemstack).getFluid())
								{
									toExport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluidStack.getFluid().getID()));
									break outerloop;
								}
								if (itemstack.getItem() instanceof IFluidContainerItem && ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack) != null && fluidstack.getFluid() == ((IFluidContainerItem) itemstack.getItem()).getFluid(itemstack).getFluid())
								{
									toExport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluidStack.getFluid().getID()));
									break outerloop;
								}
							}
						}
					}
				}

				if (toExport != null)
				{
					int filledAmount = tank.fill(facing, new FluidStack(FluidRegistry.getFluid(toExport.getItemDamage()), 20), false);

					if (filledAmount == 20)
					{
						if (grid.useMEEnergy(12.0F, "Export Fluid"))
						{
							tank.fill(facing, new FluidStack(FluidRegistry.getFluid(toExport.getItemDamage()), 20), true);
							grid.getCellArray().extractItems(toExport);
						}
					} else
					{
						tank.fill(facing, new FluidStack(FluidRegistry.getFluid(toExport.getItemDamage()), filledAmount), false);
					}
				}
			}
		}
	}

	public boolean getRedstoneAction()
	{
		return redstoneAction;
	}

	public void toggleRedstoneAction(String playerName)
	{
		redstoneAction = !redstoneAction;
		updateGuiTile(playerName);
	}

	public void updateGuiTile(String playername)
	{
		Player player = (Player) worldObj.getPlayerEntityByName(playername);

		if (!worldObj.isRemote)
			PacketDispatcher.sendPacketToPlayer(getDescriptionPacket(), player);
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

	public void updateFluids()
	{
		fluidsInNetwork = new ArrayList<SpecialFluidStack>();

		if (grid != null)
		{
			IMEInventoryHandler cellArray = grid.getCellArray();
			IItemList itemsInNetwork = null;
			if (cellArray != null)
				itemsInNetwork = cellArray.getAvailableItems();

			if (itemsInNetwork != null)
			{
				for (IAEItemStack itemstack : itemsInNetwork)
				{
					if (itemstack.getItem() == extracells.Extracells.FluidDisplay)
					{
						fluidsInNetwork.add(new SpecialFluidStack(itemstack.getItemDamage(), itemstack.getStackSize()));
					}
				}
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
	}

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		updateFluids();
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
}
