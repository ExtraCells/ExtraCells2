package extracells.tileentity;

import static extracells.ItemEnum.FLUIDDISPLAY;

import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
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
import cpw.mods.fml.common.network.PacketDispatcher;
import extracells.BlockEnum;
import extracells.util.ECPrivateInventory;

@SuppressWarnings("deprecation")
public class TileEntityLevelEmitterFluid extends ColorableECTile implements IGridMachine, IDirectionalMETile, ITileCable, IStorageAware
{
	private Boolean powerStatus = true, networkReady = true;
	private IGridInterface grid;
	private long currentAmount = 0, filterAmount = 0;
	private String customName = StatCollector.translateToLocal("tile.block.fluid.levelemitter");
	private ECPrivateInventory inventory = new ECPrivateInventory(customName, 1, 1);
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
		if (inventory.slots.get(0) != null && iss != null)
		{
			for (IAEItemStack currentStack : iss)
			{
				if (currentStack != null)
				{
					if (currentStack.getItem() == FLUIDDISPLAY.getItemInstance())
					{
						if (currentStack.getItemDamage() == inventory.slots.get(0).getItemDamage())
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
		} else if (inventory.slots.get(0) == null)
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

		nbt.setTag("Items", inventory.writeToNBT());
		if (getInventory().isInvNameLocalized())
		{
			nbt.setString("CustomName", this.customName);
		}

		nbt.setInteger("RedstoneMode", getRedstoneAction().ordinal());
		nbt.setLong("filterAmount", filterAmount);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		NBTTagList nbttaglist = nbt.getTagList("Items");
		inventory.readFromNBT(nbttaglist);
		if (nbt.hasKey("CustomName"))
		{
			this.customName = nbt.getString("CustomName");
		}

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
