package extracells.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.IMEPowerStorage;
import appeng.api.me.util.IGridInterface;

public class TileEntityMEBattery extends TileEntity implements IGridTileEntity
{

	public double energy = 0.0D;
	public final double maxEnergy = 2000000.0D;
	public final float takeEnergy = 10.0F;
	public Boolean hasPower = false;
	public IGridInterface grid;
	public Boolean storingPower = true;

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
					if (energy + takeEnergy < maxEnergy && controller.useMEEnergy(takeEnergy, "ME Backup Battery"))
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

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent((TileEntityMEBattery) this, this.worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord)));
	}

	@Override
	public void invalidate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent((TileEntityMEBattery) this, this.worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord)));
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
