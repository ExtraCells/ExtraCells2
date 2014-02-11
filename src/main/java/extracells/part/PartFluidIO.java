package extracells.part;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.Upgrades;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollsionHelper;
import appeng.api.parts.IPartRenderHelper;
import extracells.container.ContainerBusIOFluid;
import extracells.gui.GuiBusIOFluid;
import extracells.network.packet.PacketBusIOFluid;
import extracells.util.ECPrivateInventory;
import io.netty.buffer.ByteBuf;
import javafx.util.Pair;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PartFluidIO extends PartECBase implements IGridTickable, ECPrivateInventory.IInventoryUpdateReceiver
{
	protected Fluid[] filterFluids = new Fluid[9];
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	protected byte filterSize;
	protected byte speedState;
	protected boolean redstoneControlled;
	private boolean lastRedstone;
	private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 4, 1, this)
	{
		public boolean isItemValidForSlot(int i, ItemStack itemstack)
		{
			if (itemstack == null)
				return false;
			if (itemstack.isItemEqual(AEApi.instance().materials().materialCardCapacity.stack(1)))
				return true;
			else if (itemstack.isItemEqual(AEApi.instance().materials().materialCardSpeed.stack(1)))
				return true;
			else if (itemstack.isItemEqual(AEApi.instance().materials().materialCardRedstone.stack(1)))
				return true;
			return false;
		}
	};

	@Override
	public abstract void renderInventory(IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public abstract void renderStatic(int x, int y, int z, IPartRenderHelper rh, RenderBlocks renderer);

	@Override
	public final void renderDynamic(double x, double y, double z, IPartRenderHelper rh, RenderBlocks renderer)
	{
	}

	public ECPrivateInventory getUpgradeInventory()
	{
		return upgradeInventory;
	}

	@Override
	public final void writeToNBT(NBTTagCompound data)
	{
		data.setInteger("redstoneMode", redstoneMode.ordinal());
		for (int i = 0; i < filterFluids.length; i++)
		{
			Fluid fluid = filterFluids[i];
			if (fluid != null)
				data.setString("FilterFluid#" + i, fluid.getName());
			else
				data.setString("FilterFluid#" + i, "");
		}
		data.setTag("upgradeInventory", upgradeInventory.writeToNBT());
	}

	@Override
	public final void readFromNBT(NBTTagCompound data)
	{
		redstoneMode = RedstoneMode.values()[data.getInteger("redstoneMode")];
		for (int i = 0; i < 9; i++)
		{
			filterFluids[i] = FluidRegistry.getFluid(data.getString("FilterFluid#" + i));
		}
		upgradeInventory.readFromNBT(data.getTagList("upgradeInventory", 10));// TODO
		onInventoryChanged();
	}

	@Override
	public final void writeToStream(ByteBuf data) throws IOException
	{
	}

	@Override
	public final boolean readFromStream(ByteBuf data) throws IOException
	{
		return false;
	}

	@Override
	public abstract void getBoxes(IPartCollsionHelper bch);

	@Override
	public int cableConnectionRenderTo()
	{
		return 5;
	}

	@Override
	public final TickingRequest getTickingRequest(IGridNode node)
	{
		return new TickingRequest(1, 20, false, false);
	}

	@Override
	public final TickRateModulation tickingRequest(IGridNode node, int TicksSinceLastCall)
	{
		if (canDoWork())
			return doWork(speedState * 125, TicksSinceLastCall) ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
		return TickRateModulation.SLOWER;
	}

	public abstract boolean doWork(int rate, int TicksSinceLastCall);

	public final void setFilterFluid(int index, Fluid fluid, EntityPlayer player)
	{
		filterFluids[index] = fluid;
		new PacketBusIOFluid(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
	}

	public RedstoneMode getRedstoneMode()
	{
		return redstoneMode;
	}

	public void loopRedstoneMode(EntityPlayer player)
	{
		if (redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
			redstoneMode = RedstoneMode.values()[redstoneMode.ordinal() + 1];
		else
			redstoneMode = RedstoneMode.values()[0];
		new PacketBusIOFluid(redstoneMode).sendPacketToPlayer(player);
	}

	public Object getServerGuiElement(EntityPlayer player)
	{
		return new ContainerBusIOFluid(this, player);
	}

	public Object getClientGuiElement(EntityPlayer player)
	{
		return new GuiBusIOFluid(this, player);
	}

	public void sendInformation(EntityPlayer player)
	{
		new PacketBusIOFluid(Arrays.asList(filterFluids)).sendPacketToPlayer(player);
		new PacketBusIOFluid(redstoneMode).sendPacketToPlayer(player);
		new PacketBusIOFluid(filterSize).sendPacketToPlayer(player);
	}

	@Override
	public void onInventoryChanged()
	{
		filterSize = 0;
		redstoneControlled = false;
		speedState = 0;
		for (int i = 0; i < upgradeInventory.getSizeInventory(); i++)
		{
			ItemStack currentStack = upgradeInventory.getStackInSlot(i);
			if (currentStack != null)
			{
				if (currentStack.isItemEqual(AEApi.instance().materials().materialCardCapacity.stack(1)))
					filterSize++;
				if (currentStack.isItemEqual(AEApi.instance().materials().materialCardRedstone.stack(1)))
					redstoneControlled = true;
				if (currentStack.isItemEqual(AEApi.instance().materials().materialCardSpeed.stack(1)))
					speedState++;
			}
		}
		new PacketBusIOFluid(filterSize).sendPacketToAllPlayers();
		new PacketBusIOFluid(redstoneControlled).sendPacketToAllPlayers();
	}

	private boolean canDoWork()
	{
		if (!redstoneControlled)
			return true;
		switch (getRedstoneMode())
		{
		case IGNORE:
			return true;
		case LOW_SIGNAL:
			return !redstonePowered;
		case HIGH_SIGNAL:
			return redstonePowered;
		case SIGNAL_PULSE:
			if (!redstonePowered)
			{
				lastRedstone = false;
			} else
			{
				if (!lastRedstone)
				{
					return true;
				} else
				{
					lastRedstone = true;
					return true;
				}
			}
			break;
		}
		return false;
	}

	public static List<Pair<Upgrades, Integer>> getPossibleUpgrades()
	{
		List<Pair<Upgrades, Integer>> pairList = new ArrayList<Pair<Upgrades, Integer>>();
		pairList.add(new Pair<Upgrades, Integer>(Upgrades.CAPACITY, 2));
		pairList.add(new Pair<Upgrades, Integer>(Upgrades.REDSTONE, 1));
		pairList.add(new Pair<Upgrades, Integer>(Upgrades.SPEED, 2));
		return pairList;
	}
}
