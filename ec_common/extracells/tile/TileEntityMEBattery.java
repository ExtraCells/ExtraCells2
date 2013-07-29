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

public class TileEntityMEBattery extends TileEntity implements IMEPowerStorage, IGridTileEntity
{

	public float energy = 0.0F;
	public final float maxEnergy = 10000.0F;
	public Boolean hasPower = false;
	public IGridInterface grid;

	@Override
	public boolean useMEEnergy(float use, String for_what)
	{
		if (energy - use >= 0.0F)
		{
			energy -= use;
			return true;
		} else
		{
			return false;
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
	public double addMEPower(double amt)
	{
		if (((double) energy + amt) <= (double) maxEnergy)
		{
			return amt;
		} else
		{
			double left = amt - ((double) maxEnergy - (double) energy);
			energy = maxEnergy;
			return left;
		}
	}

	@Override
	public double getMEMaxPower()
	{
		return (double) maxEnergy;
	}

	@Override
	public double getMECurrentPower()
	{
		return (double) energy;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		energy = nbt.getFloat("storedEnergy");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setFloat("storedEnergy", energy);
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
