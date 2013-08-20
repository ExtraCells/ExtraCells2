package extracells.tile;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;

public class TileEntityBusFluidImport extends TileEntity implements IGridMachine, IDirectionalMETile
{
	Boolean powerStatus = false;
	IGridInterface grid;
	ItemStack[] filterSlots = new ItemStack[8];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.bus.import");
	ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 1);

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
				FluidStack fluidStack = tank.getTankInfo(facing)[0].fluid;

				if (fluidStack != null)
				{
					Fluid fluid = tank.getTankInfo(facing)[0].fluid.getFluid();

					if (arrayContains(filterSlots, new ItemStack(extracells.Extracells.FluidDisplay, 1, fluid.getID())))
					{
						IAEItemStack toImport = Util.createItemStack(new ItemStack(extracells.Extracells.FluidDisplay, 20, fluid.getID()));
						toImport.setStackSize(tank.drain(facing, new FluidStack(fluid, 20), false).amount);
						IAEItemStack notImported = grid.getCellArray().addItems(toImport.copy());
						IAEItemStack imported = toImport.copy();

						if (notImported != null)
							imported.setStackSize(toImport.getStackSize() - notImported.getStackSize());

						if (imported != null && grid.useMEEnergy(12.0F, "Import Fluid"))
							for (int i = 0; i < (int) imported.getStackSize() / 10; i++)
							{
								tank.drain(facing, new FluidStack(fluid, 10), true);
							}
					}
				}
			}
		}
	}

	private Boolean arrayContains(ItemStack[] array, ItemStack itemstack)
	{
		for (ItemStack entry : array)
		{
			if (entry != null && entry.getItem() == itemstack.getItem() && entry.getItemDamage() == itemstack.getItemDamage())
				return true;
		}
		return false;
	}

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent(this, worldObj, getLocation()));
	}

	@Override
	public void invalidate()
	{
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

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}
}
