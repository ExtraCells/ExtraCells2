package extracells.part.fluid;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.Vec3d;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartHost;
import appeng.api.parts.PartItemStack;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import appeng.api.util.DimensionalCoord;
import extracells.container.IUpgradeable;
import extracells.container.fluid.ContainerBusFluidIO;
import extracells.gui.fluid.GuiBusFluidIO;
import extracells.gui.widget.fluid.IFluidSlotListener;
import extracells.inventory.IInventoryListener;
import extracells.inventory.InventoryPlain;
import extracells.inventory.UpgradeInventory;
import extracells.network.packet.other.PacketFluidSlotUpdate;
import extracells.network.packet.part.PacketPartConfig;
import extracells.part.PartECBase;
import extracells.util.NetworkUtil;
import io.netty.buffer.ByteBuf;

public abstract class PartFluidIO extends PartECBase implements IGridTickable, IInventoryListener, IFluidSlotListener, IUpgradeable {

	public final Fluid[] filterFluids = new Fluid[9];
	private final UpgradeInventory upgradeInventory = new UpgradeInventory(this){
		@Override
		protected void onContentsChanged() {
			saveData();
		}
	};
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	protected byte filterSize;
	protected byte speedState;
	protected boolean redstoneControlled;
	//private boolean lastRedstone;

	@Override
	public void getDrops(List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : upgradeInventory.slots) {
			if (stack == null) {
				continue;
			}
			drops.add(stack);
		}
	}

	@Override
	public ItemStack getItemStack(PartItemStack type) {
		ItemStack stack = super.getItemStack(type);
		if (type.equals(PartItemStack.WRENCH)) {
			stack.getTagCompound().removeTag("upgradeInventory");
		}
		return stack;
	}


	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 5.0F;
	}

	private boolean canDoWork() {
		boolean redstonePowered = isRedstonePowered();
		if (!this.redstoneControlled) {
			return true;
		}
		switch (getRedstoneMode()) {
			case IGNORE:
				return true;
			case LOW_SIGNAL:
				return !redstonePowered;
			case HIGH_SIGNAL:
				return redstonePowered;
			case SIGNAL_PULSE:
				return false;
		}
		return false;
	}

	public byte getSpeedState() {
		return this.speedState;
	}

	public abstract boolean doWork(int rate, int TicksSinceLastCall);

	@Override
	public abstract void getBoxes(IPartCollisionHelper bch);

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiBusFluidIO(this, player);
	}

	@Override
	public int getLightLevel() {
		return 0;
	}

	public RedstoneMode getRedstoneMode() {
		return this.redstoneMode;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerBusFluidIO(this, player);
	}

	@Override
	public final TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	public InventoryPlain getUpgradeInventory() {
		return this.upgradeInventory;
	}

	@Override
	public List<String> getWailaBodey(NBTTagCompound tag, List<String> oldList) {
		if (tag.hasKey("speed")) {
			oldList.add(tag.getInteger("speed") + "mB/t");
		} else {
			oldList.add("125mB/t");
		}
		return oldList;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		tag.setInteger("speed", 125 + this.speedState * 125);
		return tag;
	}

	public void loopRedstoneMode(EntityPlayer player) {
		if (this.redstoneMode.ordinal() + 1 < RedstoneMode.values().length) {
			this.redstoneMode = RedstoneMode.values()[this.redstoneMode.ordinal() + 1];
		} else {
			this.redstoneMode = RedstoneMode.values()[0];
		}
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE_MODE, redstoneMode.toString()), player);
		saveData();
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand enumHand, Vec3d pos) {
		boolean activate = super.onActivate(player, enumHand, pos);
		onInventoryChanged();
		return activate;
	}

	public List<Fluid> getActiveFilters() {
		List<Fluid> filter = new ArrayList<Fluid>();
		filter.add(this.filterFluids[4]);

		if (this.filterSize >= 1) {
			for (byte i = 1; i < 9; i += 2) {
				if (i != 4) {
					filter.add(this.filterFluids[i]);
				}
			}
		}

		if (this.filterSize >= 2) {
			for (byte i = 0; i < 9; i += 2) {
				if (i != 4) {
					filter.add(this.filterFluids[i]);
				}
			}
		}
		return filter;
	}

	@Override
	public void onInventoryChanged() {
		this.filterSize = 0;
		this.redstoneControlled = false;
		this.speedState = 0;
		for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
			ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
			if (currentStack != null) {
				if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack)) {
					this.filterSize++;
				}
				if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(currentStack)) {
					this.redstoneControlled = true;
				}
				if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack)) {
					this.speedState++;
				}
			}
		}

		IPartHost host = getHost();
		DimensionalCoord coord = getLocation();
		if (host == null
			|| coord == null
			|| coord.getWorld() == null
			|| coord.getWorld().isRemote) {
			return;
		}
		NetworkUtil.sendNetworkPacket(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_FILTER, Byte.toString(filterSize)), coord.getPos(), coord.getWorld());
		NetworkUtil.sendNetworkPacket(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE, Boolean.toString(redstoneControlled)), coord.getPos(), coord.getWorld());
		saveData();
	}

	@Override
	public void onNeighborChanged() {
		super.onNeighborChanged();
		/*boolean redstonePowered = isRedstonePowered();
		this.lastRedstone = redstonePowered;*/
	}

	@Override
	public final void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.redstoneMode = RedstoneMode.values()[data
			.getInteger("redstoneMode")];
		for (int i = 0; i < 9; i++) {
			this.filterFluids[i] = FluidRegistry.getFluid(data
				.getString("FilterFluid#" + i));
		}
		this.upgradeInventory.readFromNBT(data.getTagList("upgradeInventory",
			10));
		onInventoryChanged();
	}

	@Override
	public final boolean readFromStream(ByteBuf data) throws IOException {
		return super.readFromStream(data);
	}

	public void sendInformation(EntityPlayer player) {
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(Arrays.asList(this.filterFluids)), player);
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_FILTER, Byte.toString(filterSize)), player);
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE, Boolean.toString(redstoneControlled)), player);
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_IO_REDSTONE_MODE, this.redstoneMode.toString()), player);
	}

	@Override
	public final void setFluid(int index, Fluid fluid, EntityPlayer player) {
		this.filterFluids[index] = fluid;
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(Arrays.asList(this.filterFluids)), player);
		saveData();
	}

	@Override
	public void setPartHostInfo(AEPartLocation location, IPartHost iPartHost, TileEntity tileEntity) {
		super.setPartHostInfo(location, iPartHost, tileEntity);
		onInventoryChanged();
	}

	@Override
	public final TickRateModulation tickingRequest(IGridNode node,
		int TicksSinceLastCall) {
		if (canDoWork()) {
			return doWork(125 + this.speedState * 125, TicksSinceLastCall) ? TickRateModulation.FASTER
				: TickRateModulation.SLOWER;
		}
		return TickRateModulation.SLOWER;
	}

	@Override
	public final void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setInteger("redstoneMode", this.redstoneMode.ordinal());
		for (int i = 0; i < this.filterFluids.length; i++) {
			Fluid fluid = this.filterFluids[i];
			if (fluid != null) {
				data.setString("FilterFluid#" + i, fluid.getName());
			} else {
				data.setString("FilterFluid#" + i, "");
			}
		}
		data.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
	}

	@Override
	public final void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
	}
}
