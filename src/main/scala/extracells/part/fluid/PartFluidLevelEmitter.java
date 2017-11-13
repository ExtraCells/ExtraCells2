package extracells.part.fluid;

import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IStorageChannel;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Random;

import extracells.api.gas.IAEGasStack;
import extracells.util.StorageChannels;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.common.FMLCommonHandler;

import appeng.api.AEApi;
import appeng.api.config.RedstoneMode;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.storage.IStackWatcher;
import appeng.api.networking.storage.IStackWatcherHost;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AECableType;
import appeng.api.util.AEPartLocation;
import extracells.container.fluid.ContainerFluidEmitter;
import extracells.gui.fluid.GuiFluidEmitter;
import extracells.gui.widget.fluid.IFluidSlotListener;
import extracells.models.PartModels;
import extracells.network.packet.other.PacketFluidSlotUpdate;
import extracells.network.packet.part.PacketPartConfig;
import extracells.part.PartECBase;
import extracells.util.NetworkUtil;
import extracells.util.PermissionUtil;
import io.netty.buffer.ByteBuf;

public class PartFluidLevelEmitter extends PartECBase implements IStackWatcherHost, IFluidSlotListener {

	protected Fluid selectedFluid;
	private RedstoneMode mode = RedstoneMode.HIGH_SIGNAL;
	private IStackWatcher watcher;
	private long wantedAmount;
	protected long currentAmount;
	private boolean clientRedstoneOutput = false;
	protected boolean isGas = false;

	@Override
	public float getCableConnectionLength(AECableType aeCableType) {
		return 16.0F;
	}

	@Override
	public AECableType getCableConnectionType(final AEPartLocation dir) {
		return AECableType.SMART;
	}

	public void changeWantedAmount(int modifier, EntityPlayer player) {
		setWantedAmount(this.wantedAmount + modifier, player);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(7, 7, 11, 9, 9, 16);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiFluidEmitter(this, player);
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerFluidEmitter(this, player);
	}

	private boolean isLevelEmitterOn() {
		if (FMLCommonHandler.instance().getEffectiveSide().isClient()) {
			return clientRedstoneOutput;
		}
		IGridNode gridNode = getGridNode();
		if (gridNode == null || !gridNode.isActive()) {
			return false;
		}
		switch (this.mode) {
			case LOW_SIGNAL:
				return this.wantedAmount >= this.currentAmount;
			case HIGH_SIGNAL:
				return this.wantedAmount <= this.currentAmount;
			default:
				return false;
		}
	}

	@Override
	public int isProvidingStrongPower() {
		return isLevelEmitterOn() ? 15 : 0;
	}

	@Override
	public int isProvidingWeakPower() {
		return isProvidingStrongPower();
	}

	protected void notifyTargetBlock(TileEntity tileEntity, EnumFacing facing) {
		// note - params are always the same
		tileEntity.getWorld().notifyNeighborsOfStateChange(tileEntity.getPos(), Blocks.AIR, true);
		tileEntity.getWorld().notifyNeighborsOfStateChange(tileEntity.getPos().offset(facing), Blocks.AIR, true);
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (IPart) this)) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	@Override
	public void onStackChange(IItemList o, IAEStack fullStack, IAEStack diffStack, IActionSource src, IStorageChannel chan) {
		if (isGas && chan == StorageChannels.GAS())
			onStackChangeGas((IAEGasStack) fullStack, (IAEGasStack) diffStack);
		else if (chan == StorageChannels.FLUID() && diffStack != null && ((IAEFluidStack) diffStack).getFluid() == this.selectedFluid) {
			this.currentAmount = fullStack != null ? fullStack.getStackSize() : 0;

			IGridNode node = getGridNode();
			if (node != null) {
				setActive(node.isActive());
				getHost().markForUpdate();
				notifyTargetBlock(getHostTile(), getFacing());
			}
		}
	}

	protected void onStackChangeGas(IAEGasStack fullStack, IAEGasStack diffStack){

	}

	@Override
	public void randomDisplayTick(World world, BlockPos blockPos, Random random) {
		if (isLevelEmitterOn()) {
			EnumFacing facing = getFacing();
			double d0 = facing.getFrontOffsetX() * 0.45F + (random.nextFloat() - 0.5F) * 0.2D;
			double d1 = facing.getFrontOffsetY() * 0.45F + (random.nextFloat() - 0.5F) * 0.2D;
			double d2 = facing.getFrontOffsetZ() * 0.45F + (random.nextFloat() - 0.5F) * 0.2D;
			world.spawnParticle(EnumParticleTypes.REDSTONE, 0.5 + blockPos.getX() + d0, 0.5 + blockPos.getY() + d1, 0.5 + blockPos.getZ() + d2, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		this.selectedFluid = FluidRegistry.getFluid(data.getString("fluid"));
		this.mode = RedstoneMode.values()[data.getInteger("mode")];
		this.wantedAmount = data.getLong("wantedAmount");
		if (this.wantedAmount < 0) {
			this.wantedAmount = 0;
		}
	}

	@Override
	public boolean readFromStream(ByteBuf data) throws IOException {
		super.readFromStream(data);
		this.clientRedstoneOutput = data.readBoolean();
		if (getHost() != null) {
			getHost().markForUpdate();
		}
		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		if (this.selectedFluid != null) {
			data.setString("fluid", this.selectedFluid.getName());
		} else {
			data.removeTag("fluid");
		}
		data.setInteger("mode", this.mode.ordinal());
		data.setLong("wantedAmount", this.wantedAmount);
	}

	@Override
	public void writeToStream(ByteBuf data) throws IOException {
		super.writeToStream(data);
		data.writeBoolean(isLevelEmitterOn());
	}

	@Override
	public void setFluid(int index, Fluid fluid, EntityPlayer player) {
		this.selectedFluid = fluid;
		if (this.watcher == null) {
			return;
		}
		this.watcher.reset();
		updateWatcher(this.watcher);
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.selectedFluid)), player);
		saveData();
	}

	public void setWantedAmount(long wantedAmount, EntityPlayer player) {
		this.wantedAmount = wantedAmount;
		if (this.wantedAmount < 0) {
			this.wantedAmount = 0;
		}
		notifyTargetBlock(getHostTile(), getFacing());
		if (getHost() != null) {
			getHost().markForUpdate();
		}
		saveData();
	}

	public void syncClientGui(EntityPlayer player) {
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_MODE, mode.toString()), player);
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_AMOUNT, Long.toString(wantedAmount)), player);
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.selectedFluid)), player);
	}

	public long getWantedAmount() {
		return wantedAmount;
	}

	public void toggleMode(EntityPlayer player) {
		switch (this.mode) {
			case LOW_SIGNAL:
				this.mode = RedstoneMode.HIGH_SIGNAL;
				break;
			default:
				this.mode = RedstoneMode.LOW_SIGNAL;
				break;
		}

		notifyTargetBlock(getHostTile(), getFacing());
		NetworkUtil.sendToPlayer(new PacketPartConfig(this, PacketPartConfig.FLUID_EMITTER_MODE, mode.toString()), player);
		if (getHost() != null) {
			getHost().markForUpdate();
		}
		saveData();
	}

	@Override
	public void updateWatcher(IStackWatcher newWatcher) {
		this.watcher = newWatcher;
		if (this.selectedFluid != null) {
			if(isGas)
				this.watcher.add(StorageChannels.GAS().createStack(new FluidStack(this.selectedFluid, 1)));
			else
				this.watcher.add(StorageChannels.FLUID().createStack(new FluidStack(this.selectedFluid, 1)));
		}
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return isLevelEmitterOn() ? PartModels.EMITTER_ON_HAS_CHANNEL : PartModels.EMITTER_OFF_HAS_CHANNEL;
		} else if (isPowered()) {
			return isLevelEmitterOn() ? PartModels.EMITTER_ON_ON : PartModels.EMITTER_OFF_ON;
		} else {
			return isLevelEmitterOn() ? PartModels.EMITTER_ON_OFF : PartModels.EMITTER_OFF_OFF;
		}
	}
}
