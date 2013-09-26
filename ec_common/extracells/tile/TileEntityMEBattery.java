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
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.IMEPowerStorage;
import appeng.api.me.util.IGridInterface;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.server.FMLServerHandler;

public class TileEntityMEBattery extends TileEntity implements IGridTileEntity
{

	private double energy;
	private final double maxEnergy = 2000000.0D;
	private final float takeEnergy = 10.0F;
	private Boolean hasPower = false;
	private IGridInterface grid;
	private Boolean storingPower = true;

	@Override
	public void updateEntity()
	{
		storingPower = this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord) || this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord);
		if (getGrid() != null)
		{
			IMEPowerStorage controller = (IMEPowerStorage) getGrid().getController();
			if (storingPower)
			{
				energy = controller.addMEPower(energy);
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
		this.hasPower = hasPower;

	}

	@Override
	public boolean isPowered()
	{
		return hasPower;
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
}
