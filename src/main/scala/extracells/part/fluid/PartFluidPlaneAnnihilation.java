package extracells.part.fluid;

import appeng.api.networking.IGridNode;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import extracells.util.MachineSource;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.gridblock.ECBaseGridBlock;
import extracells.models.PartModels;
import extracells.part.PartECBase;
import extracells.util.AEUtils;
import extracells.util.PermissionUtil;

public class PartFluidPlaneAnnihilation extends PartECBase implements IGridTickable {

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 2.0F;
	}

	@Override
	public void getBoxes(IPartCollisionHelper bch) {
		bch.addBox(2, 2, 14, 14, 14, 16);
		bch.addBox(5, 5, 13, 11, 11, 14);
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
	public boolean onActivate(EntityPlayer player, EnumHand hand, Vec3d pos) {
		if (PermissionUtil.hasPermission(player, SecurityPermissions.BUILD,
			(IPart) this)) {
			return super.onActivate(player, hand, pos);
		}
		return false;
	}

	@Override
	public void onNeighborChanged(IBlockAccess var1, BlockPos var2, BlockPos var3) {
		TileEntity hostTile = getHostTile();
		ECBaseGridBlock gridBlock = getGridBlock();
		if (hostTile == null || gridBlock == null) {
			return;
		}
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null) {
			return;
		}
		World world = hostTile.getWorld();
		BlockPos pos = hostTile.getPos();
		EnumFacing facing = getFacing();
		BlockPos offsetPos = pos.offset(facing);
		IBlockState blockState = world.getBlockState(offsetPos);
		Block fluidBlock = blockState.getBlock();
		int meta = fluidBlock.getMetaFromState(blockState);

		if (fluidBlock instanceof IFluidBlock) {
			IFluidBlock block = (IFluidBlock) fluidBlock;
			FluidStack drained = block.drain(world, offsetPos, false);
			if (drained == null) {
				return;
			}
			IAEFluidStack toInject = AEUtils.createFluidStack(drained);
			IAEFluidStack notInjected = monitor.injectItems(toInject,
				Actionable.SIMULATE, new MachineSource(this));
			if (notInjected != null) {
				return;
			}
			monitor.injectItems(toInject, Actionable.MODULATE, new MachineSource(this));
			block.drain(world, offsetPos, true);
		} else if (meta == 0) {
			IAEFluidStack toInject = getFluidStack(fluidBlock);
			if (toInject != null) {
				IAEFluidStack notInjected = monitor.injectItems(toInject, Actionable.SIMULATE, new MachineSource(this));
				if (notInjected != null) {
					return;
				}
				monitor.injectItems(toInject, Actionable.MODULATE, new MachineSource(this));
				world.setBlockToAir(offsetPos);
			}
		}
	}

	private static IAEFluidStack getFluidStack(Block fluidBlock) {
		return fluidBlock == Blocks.WATER || fluidBlock == Blocks.FLOWING_WATER ? AEUtils.createFluidStack(FluidRegistry.WATER) :
                fluidBlock == Blocks.LAVA || fluidBlock == Blocks.FLOWING_LAVA ? AEUtils.createFluidStack(FluidRegistry.LAVA) :
                        null;
	}

	@Override
	public IPartModel getStaticModels() {
		if (isActive() && isPowered()) {
			return PartModels.ANNIHILATION_PLANE_HAS_CHANNEL;
		} else if (isPowered()) {
			return PartModels.ANNIHILATION_PLANE_ON;
		} else {
			return PartModels.ANNIHILATION_PLANE_OFF;
		}
	}

	@SuppressWarnings("unused")
	@MENetworkEventSubscribe
	public void channelChanged(MENetworkChannelChanged e) {
		if (e.node == getGridNode()) {
			onNeighborChanged();
		}
	}

	@Override
	@SuppressWarnings("unused")
	@MENetworkEventSubscribe
	public void setPower(MENetworkPowerStatusChange notUsed) {
		super.setPower(notUsed);
		onNeighborChanged();
	}

    @Override
    public TickingRequest getTickingRequest(IGridNode iGridNode) {
        return new TickingRequest(1, 20, false, false);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode iGridNode, int i) {
        onNeighborChanged();
        return TickRateModulation.SAME;
    }
}
