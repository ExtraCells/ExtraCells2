package extracells.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.IMEPowerStorage;
import appeng.api.me.util.IGridInterface;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.server.FMLServerHandler;

public class TileEntityMEBattery extends TileEntity implements IGridMachine
{

	private double energy;
	private final double maxEnergy = 2000000.0D;
	private final float takeEnergy = 10.0F;
	Boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	private Boolean rechargeNetwork = null;

	@Override
	public void updateEntity()
	{
		if (getGrid() != null)
		{
			if (rechargeNetwork == null) 
			{
				updateRechargeNetwork();
			}
			
			IMEPowerStorage controller = (IMEPowerStorage) getGrid().getController();
			if (rechargeNetwork)
			{
				if ( controller.getMECurrentPower() < controller.getMEMaxPower() ) 
				{
					energy = controller.addMEPower(energy);
				}
			} else
			{
				for (int i = 0; i < 5; i++)
				{
					if (energy + takeEnergy <= maxEnergy && controller.useMEEnergy(takeEnergy, StatCollector.translateToLocal("tile.block.mebattery")))
					{
						energy += takeEnergy;
					} else
					{
						break;
					}
				}
			}
		}
	}
	
	public void onNeighborBlockChange(World world, int i, int j, int k, int l) 
	{
		updateRechargeNetwork();
	}
	
	private void updateRechargeNetwork() 
	{
		rechargeNetwork = this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord);
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

	public double getMaxEnergy()
	{
		return maxEnergy;
	}

	public double getEnergy()
	{
		return energy;
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
		this.powerStatus = hasPower;

	}

	@Override
	public boolean isPowered()
	{
		return powerStatus;
	}

	@Override
	public IGridInterface getGrid()
	{
		return this.grid;
	}

	@Override
	public void setGrid(IGridInterface gi)
	{
		this.grid = gi;

	}

	@Override
	public World getWorld()
	{
		return this.worldObj;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 0.0F;
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
