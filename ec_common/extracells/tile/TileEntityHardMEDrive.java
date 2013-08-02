package extracells.tile;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.events.GridStorageUpdateEvent;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.ICellContainer;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;

public class TileEntityHardMEDrive extends TileEntity implements IInventory, IGridMachine, ICellContainer
{

	private Boolean hasPower;
	private IGridInterface grid;
	private String costumName = "Blastresistant ME Drive";
	private ItemStack[] oldSlots;
	private ItemStack[] driveSlots;

	public TileEntityHardMEDrive()
	{
		driveSlots = new ItemStack[3];
		oldSlots = new ItemStack[3];
	}

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent((TileEntityHardMEDrive) this, this.worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord)));
	}

	@Override
	public void invalidate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent((TileEntityHardMEDrive) this, this.worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord)));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		NBTTagList nbttaglist = new NBTTagList();

		for (int i = 0; i < this.driveSlots.length; ++i)
		{
			if (this.driveSlots[i] != null)
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				this.driveSlots[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}
		nbt.setTag("Items", nbttaglist);
		if (this.isInvNameLocalized())
		{
			nbt.setString("CustomName", this.costumName);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		this.driveSlots = new ItemStack[this.getSizeInventory()];
		if (nbt.hasKey("CustomName"))
		{
			this.costumName = nbt.getString("CustomName");
		}
		for (int i = 0; i < nbttaglist.tagCount(); ++i)
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 255;

			if (j >= 0 && j < this.driveSlots.length)
			{
				this.driveSlots[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	@Override
	public int getSizeInventory()
	{
		return 3;
	}

	@Override
	public ItemStack getStackInSlot(int i)
	{
		return driveSlots[i];
	}

	@Override
	public ItemStack decrStackSize(int i, int j)
	{
		if (this.driveSlots[i] != null)
		{
			ItemStack itemstack;
			if (this.driveSlots[i].stackSize <= j)
			{
				itemstack = this.driveSlots[i];
				this.driveSlots[i] = null;
				this.onInventoryChanged();
				return itemstack;
			} else
			{
				itemstack = this.driveSlots[i].splitStack(j);
				if (this.driveSlots[i].stackSize == 0)
				{
					this.driveSlots[i] = null;
				}
				this.onInventoryChanged();
				return itemstack;
			}
		} else
		{
			return null;
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int i)
	{
		if (this.driveSlots[i] != null)
		{
			ItemStack itemstack = this.driveSlots[i];
			this.driveSlots[i] = null;
			return itemstack;
		} else
		{
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack)
	{
		this.driveSlots[i] = itemstack;

		if (itemstack != null && itemstack.stackSize > this.getInventoryStackLimit())
		{
			itemstack.stackSize = this.getInventoryStackLimit();
		}
		this.onInventoryChanged();
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		MinecraftForge.EVENT_BUS.post(new GridStorageUpdateEvent(worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord), this.getGrid()));
	}

	@Override
	public String getInvName()
	{
		return costumName;
	}

	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	@Override
	public int getInventoryStackLimit()
	{
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public void openChest()
	{
		// NOBODY needs this!
	}

	@Override
	public void closeChest()
	{
		// NOBODY needs this!
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		return appeng.api.Util.getCellRegistry().isCellHandled(itemstack);
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

	@Override
	public List<IMEInventoryHandler> getCellArray()
	{
		if (hasPower)
		{
			IMEInventoryHandler[] cellArray = new IMEInventoryHandler[3];
			for (int i = 0; i < 3; i++)
			{
				cellArray[i] = appeng.api.Util.getCellRegistry().getHandlerForCell(this.driveSlots[i]);
			}
			return Arrays.asList(cellArray);
		} else
		{
			return null;
		}
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 2.0F;
	}
}
