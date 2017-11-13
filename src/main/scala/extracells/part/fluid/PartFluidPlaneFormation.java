package extracells.part.fluid;

import com.google.common.collect.ImmutableList;

import java.util.List;

import extracells.util.MachineSource;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.container.ContainerPlaneFormation;
import extracells.container.IUpgradeable;
import extracells.gridblock.ECBaseGridBlock;
import extracells.gui.fluid.GuiFluidPlaneFormation;
import extracells.gui.widget.fluid.IFluidSlotListener;
import extracells.inventory.InventoryPlain;
import extracells.inventory.UpgradeInventorySingle;
import extracells.models.PartModels;
import extracells.network.packet.other.PacketFluidSlotUpdate;
import extracells.part.PartECBase;
import extracells.util.AEUtils;
import extracells.util.NetworkUtil;
import extracells.util.PermissionUtil;

public class PartFluidPlaneFormation extends PartECBase implements IFluidSlotListener, IGridTickable, IUpgradeable {

	private Fluid fluid;
	// TODO redstone control
	//private RedstoneMode redstoneMode;
	private InventoryPlain upgradeInventory = new UpgradeInventorySingle(AEApi.instance().definitions().materials().cardRedstone());

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
	public float getCableConnectionLength(AECableType aeCableType) {
		return 2.0F;
	}

	public void doWork() {
		TileEntity hostTile = getHostTile();
		ECBaseGridBlock gridBlock = getGridBlock();
		EnumFacing facing = getFacing();

		if (this.fluid == null || hostTile == null || gridBlock == null || this.fluid.getBlock() == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null) {
			return;
		}
		World world = hostTile.getWorld();
		BlockPos pos = hostTile.getPos().offset(facing);
		IBlockState blockState = world.getBlockState(pos);
		Block worldBlock = blockState.getBlock();
		if (!(worldBlock != null && worldBlock.isAir(blockState, world, pos))) {
			return;
		}
		IAEFluidStack fluidStack = AEUtils.createFluidStack(this.fluid, Fluid.BUCKET_VOLUME);
		IAEFluidStack canDrain = monitor.extractItems(fluidStack, Actionable.SIMULATE, new MachineSource(this));
		if (canDrain == null || canDrain.getStackSize() < Fluid.BUCKET_VOLUME) {
			return;
		}
		monitor.extractItems(fluidStack, Actionable.MODULATE, new MachineSource(this));
		Block fluidWorldBlock = this.fluid.getBlock();
		world.setBlockState(pos, fluidWorldBlock.getDefaultState());
		world.notifyBlockUpdate(pos, fluidWorldBlock.getDefaultState(), fluidWorldBlock.getDefaultState(), 0);
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
	}

	@Override
	public Object getClientGuiElement(EntityPlayer player) {
		return new GuiFluidPlaneFormation(this, player);
	}

	@Override
	public int getLightLevel() {
		return 0;
	}

	@Override
	public double getPowerUsage() {
		return 1.0D;
	}

	@Override
	public Object getServerGuiElement(EntityPlayer player) {
		return new ContainerPlaneFormation(this, player);
	}

	@Override
	public TickingRequest getTickingRequest(IGridNode node) {
		return new TickingRequest(1, 20, false, false);
	}

	public InventoryPlain getUpgradeInventory() {
		return this.upgradeInventory;
	}

	@Override
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
			(IPart) this)) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		this.fluid = FluidRegistry.getFluid(data.getString("fluid"));
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.FORMATION_PLANE_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.FORMATION_PLANE_ON;
		} else {
			return PartModels.FORMATION_PLANE_OFF;
		}
	}

	public void sendInformation(EntityPlayer player) {
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.fluid)), player);
	}

	@Override
	public void setFluid(int index, Fluid fluid, EntityPlayer player) {
		this.fluid = fluid;
		NetworkUtil.sendToPlayer(new PacketFluidSlotUpdate(ImmutableList.of(this.fluid)), player);
		saveData();
	}

	@Override
	public TickRateModulation tickingRequest(IGridNode node,
		int TicksSinceLastCall) {
		doWork();
		return TickRateModulation.SAME;
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		data.setString("fluid", this.fluid == null ? "" : this.fluid.getName());
	}
}
