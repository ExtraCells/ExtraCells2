package extracells.util;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import appeng.api.implementations.items.IAEWrench;

public class WrenchUtil {

	public static boolean canWrench(ItemStack wrench, EntityPlayer player, BlockPos pos) {
		if (wrench == null || wrench.getItem() == null)
			return false;
		/*try { //TODO reimplement Buildcraft
			IToolWrench w = (IToolWrench) wrench.getItem();
			return w.canWrench(player, x, y, z);
		} catch (Throwable e) {}*/
		if (wrench.getItem() instanceof IAEWrench) {
			IAEWrench w = (IAEWrench) wrench.getItem();
			return w.canWrench(wrench, player, pos);
		}
		return false;
	}

	public static void wrenchUsed(ItemStack wrench, EntityPlayer player, BlockPos pos) {
		if (wrench == null || wrench.getItem() == null)
			return;
		/*try {//TODO reimplement Buildcraft
			IToolWrench w = (IToolWrench) wrench.getItem();
			w.wrenchUsed(player, x, y, z);
		} catch (Throwable e) {}*/
	}
}
