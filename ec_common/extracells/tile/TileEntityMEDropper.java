package extracells.tile;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.world.ChunkEvent.Load;
import appeng.api.IAEItemStack;
import appeng.api.TileRef;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridStorageUpdateEvent;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.exceptions.AppEngTileMissingException;
import appeng.api.me.tiles.ICellContainer;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.tiles.IPushable;
import appeng.api.me.util.IAssemblerPattern;
import appeng.api.me.util.IGridInterface;
import appeng.api.me.util.IMEInventoryHandler;

public class TileEntityMEDropper extends TileEntity implements IGridMachine, IDirectionalMETile
{

	public ItemStack todispense;
	public Boolean locked = false;
	private IGridInterface grid = null;
	protected boolean isLoaded = false;
	private Chunk cachedChunk = null;
	public boolean hasPower = false;
	public int gridIndex = 0;
	private boolean isValidFlag = true;

	public TileEntityMEDropper()
	{
		this.hasPower = false;
	}

	public void setItem(ItemStack itemstack)
	{
		todispense = itemstack;
		todispense.stackSize = 1;
	}

	public ItemStack getItem()
	{
		return todispense;
	}

	public void setLocked(Boolean locked)
	{
		this.locked = locked;
	}

	public Boolean getLocked()
	{
		return locked;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (todispense.itemID != 0)
			nbt.setInteger("ID", todispense.itemID);
		if (todispense.getItemDamage() != 0)
			nbt.setInteger("DMG", todispense.getItemDamage());
		if (todispense.stackTagCompound != null)
			nbt.setCompoundTag("NBT", todispense.stackTagCompound);

		nbt.setBoolean("LOCKED", locked);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		if (nbt.getInteger("ID") != 0)
		{
			ItemStack temp = new ItemStack(nbt.getInteger("ID"), 1, nbt.getInteger("DMG"));
			temp.setTagCompound(nbt.getCompoundTag("NBT"));
			setItem(temp);
		}
		
		setLocked(nbt.getBoolean("LOCKED"));
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
	public void onChunkUnload()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileUnloadEvent((TileEntityMEDropper) this, this.worldObj, new WorldCoord(this.xCoord, this.yCoord, this.zCoord)));
	}

	public void init()
	{
		if (!this.isLoaded)
		{
			if (this.worldObj.getBlockTileEntity(this.xCoord, this.yCoord, this.zCoord) == this)
			{
				this.isValidFlag = true;
				this.isLoaded = true;
				this.cachedChunk = null;

				if (this instanceof IGridTileEntity)
				{
					MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent((IGridTileEntity) this, this.worldObj, this.getLocation()));
				}

				this.worldObj.updateAllLightTypes(this.xCoord, this.yCoord, this.zCoord);
			}
		}
	}

	@Override
	public void setPowerStatus(boolean _hasPower)
	{
		if (this.hasPower != _hasPower)
		{
			this.hasPower = _hasPower;
		}

	}

	@Override
	public boolean isPowered()
	{
		return this.hasPower;
	}

	@Override
	public IGridInterface getGrid()
	{
		return this.grid;
	}

	@Override
	public void validate()
	{
		MinecraftForge.EVENT_BUS.post(new GridTileLoadEvent((IGridTileEntity) this, this.worldObj, this.getLocation()));
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
	public boolean canConnect(ForgeDirection dir)
	{
		return true;
	}

	@Override
	public float getPowerDrainPerTick()
	{
		return 0.5F;
	}

}
