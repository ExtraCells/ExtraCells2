package extracells.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.ItemEnum;

@SuppressWarnings("deprecation")
public class TileEntityMonitorStorageFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile, IStorageAware
{
	private IGridInterface grid;
	private boolean locked, matrixed, powerStatus = false, networkReady = true;
	private Fluid fluid;
	private int color;
	private long fluidAmount;

	public int getColor()
	{
		return color;
	}

	public void setLocked(boolean _locked)
	{
		locked = _locked;
	}

	public boolean isLocked()
	{
		return locked;
	}

	public void setMatrixed()
	{
		matrixed = true;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public boolean isMatrixed()
	{
		return matrixed;
	}

	public void setFluid(Fluid _fluid)
	{
		fluid = _fluid;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public Fluid getFluid()
	{
		return fluid;
	}

	public long getAmount()
	{
		return fluidAmount;
	}

	public void writeToNBT(NBTTagCompound nbtTag)
	{
		super.writeToNBT(nbtTag);
		nbtTag.setString("fluid", fluid != null ? fluid.getName() : "");
	}

	public void readFromNBT(NBTTagCompound nbtTag)
	{
		super.readFromNBT(nbtTag);
		fluid = FluidRegistry.getFluid(nbtTag.getString("fluid"));
	}

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		long lastAmount = fluidAmount;
		fluidAmount = 0;
		if (fluid != null)
		{
			for (IAEItemStack stack : iss)
			{
				if (stack != null && stack.getItem() == ItemEnum.FLUIDDISPLAY.getItemInstance() && stack.getItemDamage() == fluid.getID())
				{
					fluidAmount += stack.getStackSize();
				}
			}
		}
		if (lastAmount != fluidAmount)
		{
			lastAmount = fluidAmount;
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		}
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = getColorDataForPacket();
		writeToNBT(nbtTag);
		nbtTag.setBoolean("networkReady", networkReady);
		nbtTag.setBoolean("powerStatus", powerStatus);
		nbtTag.setLong("amount", fluidAmount);
		nbtTag.setInteger("meta", getBlockMetadata());
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	@Override
	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		super.onDataPacket(net, packet);
		readFromNBT(packet.data);
		networkReady = packet.data.getBoolean("networkReady");
		powerStatus = packet.data.getBoolean("powerStatus");
		fluidAmount = packet.data.getLong("amount");
		blockMetadata = packet.data.getInteger("meta");
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
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		worldObj.markBlockForRenderUpdate(xCoord, yCoord, zCoord);
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
		if (!worldObj.isRemote)
		{
			grid = gi;
			if (gi != null)
			{
				IMEInventoryHandler cellArray = gi.getCellArray();
				if (cellArray != null)
					onNetworkInventoryChange(cellArray.getAvailableItems());
			} else
			{
				setPowerStatus(false);
			}
			PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		}
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
		return 5.0F;
	}

	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;

		if (getGrid() != null)
		{
			IMEInventoryHandler cellArray = getGrid().getCellArray();
			if (cellArray != null)
				onNetworkInventoryChange(cellArray.getAvailableItems());
		}

		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}
}
