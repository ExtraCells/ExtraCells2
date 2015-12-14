package extracells.util;

import appeng.api.implementations.items.IAEWrench;
import buildcraft.api.tools.IToolWrench;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class WrenchUtil {

	public static boolean canWrench(ItemStack wrench, EntityPlayer player,
			int x, int y, int z) {
		if (wrench == null || wrench.getItem() == null)
			return false;
		try {
			IToolWrench w = (IToolWrench) wrench.getItem();
			return w.canWrench(player, x, y, z);
		} catch (Throwable e) {}
		if (wrench.getItem() instanceof IAEWrench) {
			IAEWrench w = (IAEWrench) wrench.getItem();
			return w.canWrench(wrench, player, x, y, z);
		}
		return false;
	}

	public static void wrenchUsed(ItemStack wrench, EntityPlayer player, int x,
			int y, int z) {
		if (wrench == null || wrench.getItem() == null)
			return;
		try {
			IToolWrench w = (IToolWrench) wrench.getItem();
			w.wrenchUsed(player, x, y, z);
		} catch (Throwable e) {}
	}
}
