package extracells.part;

import java.io.IOException;
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
import extracells.container.ContainerBusFluidIO;
import extracells.gui.GuiBusFluidIO;
import extracells.network.packet.other.IFluidSlotPartOrBlock;
import extracells.network.packet.other.PacketFluidSlot;
import extracells.network.packet.part.PacketBusFluidIO;
import extracells.util.inventory.ECPrivateInventory;
import extracells.util.inventory.IInventoryUpdateReceiver;
import io.netty.buffer.ByteBuf;

public abstract class PartFluidIO extends PartECBase implements IGridTickable,
		IInventoryUpdateReceiver, IFluidSlotPartOrBlock {

	public Fluid[] filterFluids = new Fluid[9];
	private RedstoneMode redstoneMode = RedstoneMode.IGNORE;
	protected byte filterSize;
	protected byte speedState;
	protected boolean redstoneControlled;
	private boolean lastRedstone;
	private ECPrivateInventory upgradeInventory = new ECPrivateInventory("", 4,
			1, this) {

		@Override
		public boolean isItemValidForSlot(int i, ItemStack itemStack) {
			if (itemStack == null)
				return false;
			if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(itemStack))
				return true;
			else if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(itemStack))
				return true;
			else if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(itemStack))
				return true;
			return false;
		}
	};

	@Override
	public void getDrops( List<ItemStack> drops, boolean wrenched) {
		for (ItemStack stack : upgradeInventory.slots) {
			if (stack == null)
				continue;
			drops.add(stack);
		}
	}

	@Override
	public ItemStack getItemStack(PartItemStack type) {
		ItemStack stack = super.getItemStack(type);
		if (type.equals(PartItemStack.WRENCH))
			stack.getTagCompound().removeTag("upgradeInventory");
		return stack;
	}


	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 5.0F;
	}

	private boolean canDoWork() {
		boolean redstonePowered = isRedstonePowered();
		if (!this.redstoneControlled)
			return true;
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
	
	public byte getSpeedState(){
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

	public ECPrivateInventory getUpgradeInventory() {
		return this.upgradeInventory;
	}

	@Override
	public List<String> getWailaBodey(NBTTagCompound tag, List<String> oldList) {
		if (tag.hasKey("speed"))
			oldList.add(tag.getInteger("speed") + "mB/t");
		else
			oldList.add("125mB/t");
		return oldList;
	}

	@Override
	public NBTTagCompound getWailaTag(NBTTagCompound tag) {
		tag.setInteger("speed", 125 + this.speedState * 125);
		return tag;
	}

	public void loopRedstoneMode(EntityPlayer player) {
		if (this.redstoneMode.ordinal() + 1 < RedstoneMode.values().length)
			this.redstoneMode = RedstoneMode.values()[this.redstoneMode.ordinal() + 1];
		else
			this.redstoneMode = RedstoneMode.values()[0];
		new PacketBusFluidIO(this.redstoneMode).sendPacketToPlayer(player);
		saveData();
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand enumHand, Vec3d pos) {
		boolean activate = super.onActivate(player, enumHand, pos);
		onInventoryChanged();
		return activate;
	}

	@Override
	public void onInventoryChanged() {
		this.filterSize = 0;
		this.redstoneControlled = false;
		this.speedState = 0;
		for (int i = 0; i < this.upgradeInventory.getSizeInventory(); i++) {
			ItemStack currentStack = this.upgradeInventory.getStackInSlot(i);
			if (currentStack != null) {
				if (AEApi.instance().definitions().materials().cardCapacity().isSameAs(currentStack))
					this.filterSize++;
				if (AEApi.instance().definitions().materials().cardRedstone().isSameAs(currentStack))
					this.redstoneControlled = true;
				if (AEApi.instance().definitions().materials().cardSpeed().isSameAs(currentStack))
					this.speedState++;
			}
		}

		try {
			if (getHost().getLocation().getWorld().isRemote)
				return;
		} catch (Throwable ignored) {}
		new PacketBusFluidIO(this.filterSize).sendPacketToAllPlayers();
		new PacketBusFluidIO(this.redstoneControlled).sendPacketToAllPlayers();
		saveData();
	}

	@Override
	public void onNeighborChanged() {
		super.onNeighborChanged();
		boolean redstonePowered = isRedstonePowered();
		this.lastRedstone = redstonePowered;
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

	/*@SideOnly(Side.CLIENT)
	@Override
	public final void renderDynamic(double x, double y, double z,
			IPartRenderHelper rh, RenderBlocks renderer) {}

	@SideOnly(Side.CLIENT)
	@Override
	public abstract void renderInventory(IPartRenderHelper rh,
			RenderBlocks renderer);

	@SideOnly(Side.CLIENT)
	@Override
	public abstract void renderStatic(int x, int y, int z,
			IPartRenderHelper rh, RenderBlocks renderer);*/

	public void sendInformation(EntityPlayer player) {
		new PacketFluidSlot(Arrays.asList(this.filterFluids))
				.sendPacketToPlayer(player);
		new PacketBusFluidIO(this.redstoneMode).sendPacketToPlayer(player);
		new PacketBusFluidIO(this.filterSize).sendPacketToPlayer(player);
	}

	@Override
	public final void setFluid(int index, Fluid fluid, EntityPlayer player) {
		this.filterFluids[index] = fluid;
		new PacketFluidSlot(Arrays.asList(this.filterFluids))
				.sendPacketToPlayer(player);
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
		if (canDoWork())
			return doWork(125 + this.speedState * 125, TicksSinceLastCall) ? TickRateModulation.FASTER
					: TickRateModulation.SLOWER;
		return TickRateModulation.SLOWER;
	}

	@Override
	public final void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		data.setInteger("redstoneMode", this.redstoneMode.ordinal());
		for (int i = 0; i < this.filterFluids.length; i++) {
			Fluid fluid = this.filterFluids[i];
			if (fluid != null)
				data.setString("FilterFluid#" + i, fluid.getName());
			else
				data.setString("FilterFluid#" + i, "");
		}
		data.setTag("upgradeInventory", this.upgradeInventory.writeToNBT());
	}

	@Override
	public final void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
	}
}
