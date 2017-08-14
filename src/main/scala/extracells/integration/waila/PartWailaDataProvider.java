package extracells.integration.waila;

import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import extracells.part.PartECBase;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;

public class PartWailaDataProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te,
			NBTTagCompound tag, World world, BlockPos pos) {
		final RayTraceResult mop = retraceBlock(world, player, pos);

		if (mop != null) {
			final IPart part = getPart(te, mop);

			if (part != null && part instanceof PartECBase) {
				tag.setTag("partEC",
						((PartECBase) part).getWailaTag(new NBTTagCompound()));
			}
		}
		return tag;
	}

	private IPart getPart(TileEntity tile, RayTraceResult traceResult) {
		if (tile instanceof IPartHost) {
			Vec3d position = traceResult.hitVec.subtract(new Vec3d(traceResult.getBlockPos()));
			final IPartHost host = (IPartHost) tile;
			final SelectedPart sp = host.selectPart(position);
			if (sp.part != null) {
				return sp.part;
			}
		}
		return null;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		TileEntity tile = accessor.getTileEntity();

		IPart part = getPart(tile, accessor.getMOP());
		if (part != null && part instanceof PartECBase) {
			NBTTagCompound tag;
			if (accessor.getNBTData() != null
					&& accessor.getNBTData().hasKey("partEC"))
				tag = accessor.getNBTData().getCompoundTag("partEC");
			else
				tag = new NBTTagCompound();
			return ((PartECBase) part).getWailaBodey(tag, currenttip);
		}

		return currenttip;
	}

	@Override
	public List<String> getWailaHead(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	@Override
	public ItemStack getWailaStack(IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return accessor.getStack();
	}

	@Override
	public List<String> getWailaTail(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		return currenttip;
	}

	private RayTraceResult retraceBlock(World world, EntityPlayerMP player, BlockPos pos) {
		IBlockState blockState = world.getBlockState(pos);

		Vec3d head = new Vec3d(player.posX, player.posY, player.posZ).addVector(0, player.getEyeHeight(), 0);
		if (player.isSneaking())
			head = head.addVector(0, -0.08, 0);
		Vec3d look = player.getLook(1.0F);
		double reach = player.interactionManager.getBlockReachDistance();
		Vec3d endVec = head.addVector(look.xCoord * reach, look.yCoord * reach,
				look.zCoord * reach);
		return blockState.collisionRayTrace(world, pos, head, endVec);
	}
}
