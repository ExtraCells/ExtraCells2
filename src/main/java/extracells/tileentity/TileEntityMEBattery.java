package extracells.tileentity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.config.ItemFlow;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.IMEPowerStorage;
import appeng.api.me.util.IGridInterface;
import appeng.api.networkevents.MENetworkPowerStorage;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

public class TileEntityMEBattery extends TileEntity implements IGridTileEntity, IMEPowerStorage
{

	private double energy = 0;
	private final double maxEnergy = 2000000.0D;
	private boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	private boolean redstoneCached = false;
	public boolean redstonePowered = false;

	public void updateEntity()
	{
		if (!redstoneCached)
		{
			redstoneCached = true;
			updateRedstone();
		}
	}

	public void updateRedstone()
	{
		boolean newRedstone = worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord);
		if (newRedstone != redstonePowered && grid != null)
		{
			if (energy > 0.001D)
			{
				getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.PROVIDE_POWER));
			} else if (energy < maxEnergy)
			{
				getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
			}
		}
		redstonePowered = newRedstone;
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
		energy = nbt.getDouble("storedEnergy");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setDouble("storedEnergy", energy);
	}

	@Override
	public WorldCoord getLocation()
	{
		return new WorldCoord(this.xCoord, this.yCoord, this.zCoord);
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

	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}

	@Override
	public boolean useMEEnergy(float use, String for_what)
	{
		if (energy > use)
		{
			energy -= use;
			onUpdatePower();
			return true;
		}
		energy = 0.0D;
		onUpdatePower();
		return false;
	}

	@Override
	public double addMEPower(double amt)
	{
		if (getGrid() == null || !canFill())
		{
			return amt;
		}
		boolean wasEmpty = energy < 0.001D;

		energy += amt;
		if (energy > getMEMaxPower())
		{
			double overheadPower = energy - getMEMaxPower();
			energy = getMEMaxPower();
			onUpdatePower();
			return overheadPower;
		}

		if (wasEmpty && energy > 0.001D)
		{
			getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.PROVIDE_POWER));
		}

		onUpdatePower();
		return 0.0D;
	}

	@Override
	public double getMEMaxPower()
	{
		return maxEnergy;
	}

	@Override
	public double getMECurrentPower()
	{
		return energy;
	}

	@Override
	public boolean isPublicPowerStorage()
	{
		return true;
	}

	@Override
	public ItemFlow getPowerFlow()
	{
		return ItemFlow.READ_WRITE;
	}

	@Override
	public double drainMEPower(double amt)
	{
		if (getGrid() == null || !canDrain())
		{
			return 0.0D;
		}
		boolean wasFull = energy >= maxEnergy;

		energy -= amt;
		if (energy < 0.0D)
		{
			amt += energy;
			energy = 0.0D;
		}

		if (energy < maxEnergy && wasFull)
		{
			getGrid().postEvent(new MENetworkPowerStorage(this, MENetworkPowerStorage.PowerEventType.REQUEST_POWER));
		}
		onUpdatePower();
		return amt;
	}

	public boolean canDrain()
	{
		return redstonePowered;
	}

	public boolean canFill()
	{
		return redstonePowered;
	}

	void onUpdatePower()
	{
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		if (worldObj != null)
			worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
	}
}
