package extracells.integration.waila;

import mcp.mobius.waila.api.IWailaConfigHandler;
import mcp.mobius.waila.api.IWailaDataAccessor;
import mcp.mobius.waila.api.IWailaDataProvider;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.List;

public class BlockWailaDataProvider implements IWailaDataProvider {

	@Override
	public NBTTagCompound getNBTData(EntityPlayerMP player, TileEntity te,
			NBTTagCompound tag, World world, int x, int y, int z) {
		if (te != null && te instanceof IWailaTile) {
			tag.setTag("WailaTile",
					((IWailaTile) te).getWailaTag(new NBTTagCompound()));
		}
		return tag;
	}

	@Override
	public List<String> getWailaBody(ItemStack itemStack,
			List<String> currenttip, IWailaDataAccessor accessor,
			IWailaConfigHandler config) {
		TileEntity tile = accessor.getTileEntity();
		NBTTagCompound tag = accessor.getNBTData();
		if (tile != null && tile instanceof IWailaTile && tag != null
				&& tag.hasKey("WailaTile")) {
			IWailaTile t = (IWailaTile) tile;
			return t.getWailaBody(currenttip, tag.getCompoundTag("WailaTile"),
					accessor.getSide());
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

}
