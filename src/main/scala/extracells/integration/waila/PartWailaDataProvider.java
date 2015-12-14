package extracells.integration.waila;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import extracells.part.PartECBase;
import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class PartWailaDataProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te,
			NBTTagCompound tag, World world, int x, int y, int z) {
		final MovingObjectPosition mop = retraceBlock(world, player, x, y, z);

		if (mop != null) {
			final IPart part = getPart(te, mop);

			if (part != null && part instanceof PartECBase) {
				tag.setTag("partEC",
						((PartECBase) part).getWailaTag(new NBTTagCompound()));
			}
		}
		return tag;
	}

	private IPart getPart(TileEntity tile, MovingObjectPosition pos) {
		if (tile instanceof IPartHost) {
			final Vec3 position = pos.hitVec.addVector(-pos.blockX,
					-pos.blockY, -pos.blockZ);
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

		IPart part = getPart(tile, accessor.getPosition());
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

	private MovingObjectPosition retraceBlock(World world,
			EntityPlayerMP player, int x, int y, int z) {
		Block block = world.getBlock(x, y, z);

		Vec3 head = Vec3.createVectorHelper(player.posX, player.posY,
				player.posZ);
		head.yCoord += player.getEyeHeight();
		if (player.isSneaking())
			head.yCoord -= 0.08;
		Vec3 look = player.getLook(1.0F);
		double reach = player.theItemInWorldManager.getBlockReachDistance();
		Vec3 endVec = head.addVector(look.xCoord * reach, look.yCoord * reach,
				look.zCoord * reach);
		return block.collisionRayTrace(world, x, y, z, head, endVec);
	}
}
