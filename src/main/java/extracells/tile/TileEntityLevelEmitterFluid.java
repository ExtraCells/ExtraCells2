package extracells.tile;

import java.util.ArrayList;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidRegistry;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.WorldCoord;
import appeng.api.config.RedstoneModeInput;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import appeng.api.me.tiles.IDirectionalMETile;
import appeng.api.me.tiles.IGridMachine;
import appeng.api.me.tiles.IStorageAware;
import appeng.api.me.tiles.ITileCable;
import appeng.api.me.util.IGridInterface;
import extracells.BlockEnum;
import extracells.ItemEnum;
import extracells.SpecialFluidStack;
import static extracells.ItemEnum.*;

public class TileEntityLevelEmitterFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile, ITileCable, IStorageAware
{
	private Boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	private long currentAmount = 0, filterAmount = 0;
	private ItemStack[] filterSlots = new ItemStack[1];
	private String costumName = StatCollector.translateToLocal("tile.block.fluid.levelemitter");
	private ECPrivateInventory inventory = new ECPrivateInventory(filterSlots, costumName, 1);
	private RedstoneModeInput redstoneAction = RedstoneModeInput.WhenOff;

	public int getRedstonePowerBySide(ForgeDirection side)
	{
		switch (redstoneAction)
		{
		case WhenOff:
			return side.getOpposite().ordinal() == getBlockMetadata() && currentAmount < filterAmount ? 15 : 0;
		case WhenOn:
			return side.getOpposite().ordinal() == getBlockMetadata() && currentAmount > filterAmount ? 15 : 0;
		default:
			return 0;
		}
	}

	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbtTag = getColorDataForPacket();
		this.writeToNBT(nbtTag);

		nbtTag.setLong("currentAmount", currentAmount);
		nbtTag.setLong("filterAmount", filterAmount);

		return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 1, nbtTag);
	}

	public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
	{
		super.onDataPacket(net, packet);
		readFromNBT(packet.data);

		currentAmount = packet.data.getLong("currentAmount");
		filterAmount = packet.data.getLong("filterAmount");

		worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
	}

	public void updateRedstoneStates()
	{
		for (ForgeDirection currentSide : ForgeDirection.values())
		{
			Block neighbor = Block.blocksList[worldObj.getBlockId(xCoord + currentSide.offsetX, yCoord + currentSide.offsetY, zCoord + currentSide.offsetZ)];
			if (neighbor != null)
				neighbor.onNeighborBlockChange(worldObj, xCoord + currentSide.offsetX, yCoord + currentSide.offsetY, zCoord + currentSide.offsetZ, BlockEnum.FLUIDLEVELEMITTER.getBlockInstance().blockID);
		}
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		worldObj.updateAllLightTypes(xCoord, yCoord, zCoord);
	}

	public void setAmount(long filterAmount)
	{
		this.filterAmount += filterAmount;
		if (this.filterAmount < 0)
			this.filterAmount = 0;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
	}

	public long getAmount()
	{
		return filterAmount;
	}

	@Override
	public void onNetworkInventoryChange(IItemList iss)
	{
		if (filterSlots[0] != null && iss != null)
		{
			for (IAEItemStack currentStack : iss)
			{
				if (currentStack != null)
				{
					if (currentStack.getItem() == FLUIDDISPLAY.getItemInstance())
					{
						if (currentStack.getItemDamage() == filterSlots[0].getItemDamage())
						{
							if (currentStack.getStackSize() != currentAmount)
							{
								currentAmount = currentStack.getStackSize();
								updateRedstoneStates();
							}
						}
					}
				}
			}
		} else if (filterSlots[0] == null)
		{
			currentAmount = 0;
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
	public boolean coveredConnections()
	{
		return false;
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

		nbt.setInteger("RedstoneMode", getRedstoneAction().ordinal());
		nbt.setLong("filterAmount", filterAmount);
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

		redstoneAction = RedstoneModeInput.values()[nbt.getInteger("RedstoneMode")];
		filterAmount = nbt.getLong("filterAmount");
	}

	public ECPrivateInventory getInventory()
	{
		return inventory;
	}

	public RedstoneModeInput getRedstoneAction()
	{
		return redstoneAction;
	}

	public void setRedstoneAction(RedstoneModeInput action)
	{
		redstoneAction = action;
		PacketDispatcher.sendPacketToAllPlayers(getDescriptionPacket());
		updateRedstoneStates();
	}
}
