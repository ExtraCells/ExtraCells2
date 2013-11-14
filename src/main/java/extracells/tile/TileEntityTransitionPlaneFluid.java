package extracells.tile;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.ItemEnum;

public class TileEntityTransitionPlaneFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile
{
	Boolean powerStatus = true, networkReady = true;
	IGridInterface grid;

	@Override
	public void updateEntity()
	{
		if (isMachineActive() && getGrid() != null)
		{
			ForgeDirection orientation = ForgeDirection.getOrientation(getBlockMetadata());

			int offsetID = worldObj.getBlockId(xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ);
			int offsetMeta = worldObj.getBlockMetadata(xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ);

			IMEInventoryHandler cellArray = getGrid().getCellArray();
			if (cellArray != null)
			{
				if (Block.blocksList[offsetID] instanceof IFluidBlock)
				{
					FluidStack simulation = ((IFluidBlock) Block.blocksList[offsetID]).drain(worldObj, xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ, false);

					if (simulation != null && cellArray.calculateItemAddition(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), simulation.amount, simulation.fluidID))) == null)
					{
						((IFluidBlock) Block.blocksList[offsetID]).drain(worldObj, xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ, true);
						cellArray.addItems(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), simulation.amount, simulation.fluidID)));
					}
				} else if (offsetID == FluidRegistry.WATER.getBlockID() && offsetMeta == 0)
				{
					if (cellArray.calculateItemAddition(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1000, FluidRegistry.WATER.getID()))) == null)
					{
						worldObj.setBlockToAir(xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ);
						cellArray.addItems(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1000, FluidRegistry.WATER.getID())));
					}
				} else if (offsetID == FluidRegistry.LAVA.getBlockID() && offsetMeta == 0)
				{
					if (cellArray.calculateItemAddition(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1000, FluidRegistry.LAVA.getID()))) == null)
					{
						worldObj.setBlockToAir(xCoord + orientation.offsetX, yCoord + orientation.offsetY, zCoord + orientation.offsetZ);
						cellArray.addItems(Util.createItemStack(new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1000, FluidRegistry.LAVA.getID())));
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
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = getColorDataForPacket();
		this.writeToNBT(nbtTag);
		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
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

	public void setNetworkReady(boolean isReady)
	{
		networkReady = isReady;
	}

	public boolean isMachineActive()
	{
		return powerStatus && networkReady;
	}
}
