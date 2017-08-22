package extracells.part.fluid;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.events.MENetworkChannelChanged;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkPowerStatusChange;
import appeng.api.networking.security.MachineSource;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import extracells.gridblock.ECBaseGridBlock;
import extracells.part.PartECBase;
import extracells.util.FluidUtil;
import extracells.util.PermissionUtil;

public class PartFluidPlaneAnnihilation extends PartECBase {

	@Override
	public float getCableConnectionLength(AECableType cable) {
		return 2.0F;
	}

	@SuppressWarnings("unused")
	@MENetworkEventSubscribe
	public void channelChanged(MENetworkChannelChanged e) {
		if (e.node == getGridNode())
			onNeighborChanged();
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
	public void onNeighborChanged() {
		TileEntity hostTile = getHostTile();
		ECBaseGridBlock gridBlock = getGridBlock();
		if (hostTile == null || gridBlock == null)
			return;
		IMEMonitor<IAEFluidStack> monitor = gridBlock.getFluidMonitor();
		if (monitor == null)
			return;
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
			if (drained == null)
				return;
			IAEFluidStack toInject = FluidUtil.createAEFluidStack(drained);
			IAEFluidStack notInjected = monitor.injectItems(toInject,
					Actionable.SIMULATE, new MachineSource(this));
			if (notInjected != null)
				return;
			monitor.injectItems(toInject, Actionable.MODULATE, new MachineSource(this));
			block.drain(world, offsetPos, true);
		} else if (meta == 0) {
			if (fluidBlock == Blocks.FLOWING_WATER) {
				IAEFluidStack toInject = FluidUtil
						.createAEFluidStack(FluidRegistry.WATER);
				IAEFluidStack notInjected = monitor.injectItems(toInject,
						Actionable.SIMULATE, new MachineSource(this));
				if (notInjected != null)
					return;
				monitor.injectItems(toInject, Actionable.MODULATE,
						new MachineSource(this));
				world.setBlockToAir(offsetPos);
			} else if (fluidBlock == Blocks.FLOWING_LAVA) {
				IAEFluidStack toInject = FluidUtil
						.createAEFluidStack(FluidRegistry.LAVA);
				IAEFluidStack notInjected = monitor.injectItems(toInject,
						Actionable.SIMULATE, new MachineSource(this));
				if (notInjected != null)
					return;
				monitor.injectItems(toInject, Actionable.MODULATE, new MachineSource(this));
				world.setBlockToAir(offsetPos);
			}
		}
	}

	/*@SideOnly(Side.CLIENT)
	@Override
	public void renderInventory(IPartRenderHelper rh, RenderBlocks renderer) {
		IIcon side = TextureManager.PANE_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderInventoryBox(renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		rh.setInvColor(AEColor.Cyan.blackVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[0],
				ForgeDirection.SOUTH, renderer);
		Tessellator.instance.setBrightness(13 << 20 | 13 << 4);
		rh.setInvColor(AEColor.Cyan.mediumVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[1],
				ForgeDirection.SOUTH, renderer);
		rh.setInvColor(AEColor.Cyan.whiteVariant);
		rh.renderInventoryFace(TextureManager.PANE_FRONT.getTextures()[2],
				ForgeDirection.SOUTH, renderer);

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderInventoryBusLights(rh, renderer);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void renderStatic(int x, int y, int z, IPartRenderHelper rh,
			RenderBlocks renderer) {
		Tessellator ts = Tessellator.instance;
		IIcon side = TextureManager.PANE_SIDE.getTexture();
		rh.setTexture(side, side, side, TextureManager.BUS_BORDER.getTexture(),
				side, side);
		rh.setBounds(2, 2, 14, 14, 14, 16);
		rh.renderBlock(x, y, z, renderer);
		rh.setBounds(3, 3, 14, 13, 13, 16);
		IPartHost host = getHost();
		if (host != null) {
			ts.setColorOpaque_I(host.getColor().blackVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[0],
					ForgeDirection.SOUTH, renderer);
			if (isActive())
				ts.setBrightness(13 << 20 | 13 << 4);
			ts.setColorOpaque_I(host.getColor().mediumVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[1],
					ForgeDirection.SOUTH, renderer);
			ts.setColorOpaque_I(host.getColor().whiteVariant);
			rh.renderFace(x, y, z, TextureManager.PANE_FRONT.getTextures()[2],
					ForgeDirection.SOUTH, renderer);
		}

		rh.setBounds(5, 5, 13, 11, 11, 14);
		renderStaticBusLights(x, y, z, rh, renderer);
	}*/

	@Override
	@SuppressWarnings("unused")
	@MENetworkEventSubscribe
	public void setPower(MENetworkPowerStatusChange notUsed) {
		super.setPower(notUsed);
		onNeighborChanged();
	}
}
