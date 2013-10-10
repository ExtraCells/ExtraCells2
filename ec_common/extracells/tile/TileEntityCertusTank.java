package extracells.tile;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fluids.IFluidTank;

public class TileEntityCertusTank extends TileEntity implements IFluidHandler
{

	protected FluidTank tank = new FluidTank(32000);

	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = new NBTTagCompound();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		readFromNBT(packet.data);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		readFromNBTWithoutCoords(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		writeToNBTWithoutCoords(tag);
	}

	public void writeToNBTWithoutCoords(NBTTagCompound tag)
	{
		tank.writeToNBT(tag);
	}

	public void readFromNBTWithoutCoords(NBTTagCompound tag)
	{
		tank.readFromNBT(tag);
	}

	/* IFluidHandler */
	@Override
	public int fill(ForgeDirection from, FluidStack resource, boolean doFill)
	{
		if (resource == null || (tank.getFluid() != null && resource.fluidID != tank.getFluid().fluidID))
			return 0;

		int filled = 0;

		if (worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord) instanceof TileEntityCertusTank)
		{
			filled += ((TileEntityCertusTank) worldObj.getBlockTileEntity(xCoord, yCoord - 1, zCoord)).fill(from, resource, doFill);
			resource.amount -= filled;
		}

		filled += tank.fill(resource, doFill);
		if (filled > 0)
			PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
		return filled;
	}

	@Override
	public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain)
	{
		if (resource == null || !resource.isFluidEqual(tank.getFluid()))
		{
			return null;
		}

		FluidStack drainedUp = null;
		if (worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord) instanceof TileEntityCertusTank)
		{
			drainedUp = ((TileEntityCertusTank) worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord)).drain(from, resource, doDrain);
			if (drainedUp != null)
			{
				resource.amount -= drainedUp.amount;
			}
		}

		FluidStack drained = resource != null ? tank.drain(resource.amount, doDrain) : null;

		if (drainedUp != null)
		{
			if (drained == null)
			{
				return drainedUp;
			} else
			{
				drained.amount += drainedUp.amount;
			}
		}
		PacketDispatcher.sendPacketToAllAround(xCoord, yCoord, zCoord, 50, worldObj.provider.dimensionId, getDescriptionPacket());
		return drained;
	}

	@Override
	public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain)
	{
		if (tank.getFluid() == null)
			return null;

		return drain(from, new FluidStack(tank.getFluid(), maxDrain), doDrain);
	}

	@Override
	public boolean canFill(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public boolean canDrain(ForgeDirection from, Fluid fluid)
	{
		return true;
	}

	@Override
	public FluidTankInfo[] getTankInfo(ForgeDirection from)
	{
		return new FluidTankInfo[]
		{ tank.getInfo() };
	}
}
