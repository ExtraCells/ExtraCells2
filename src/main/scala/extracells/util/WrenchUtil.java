package extracells.util;

import extracells.api.IWrenchHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;

import java.util.ArrayList;
import java.util.List;

public class WrenchUtil {

	private static List<IWrenchHandler> handlers = new ArrayList<IWrenchHandler>();

	public static void addWrenchHandler(IWrenchHandler handler){
		handlers.add(handler);
	}

	public static IWrenchHandler getHandler(ItemStack wrench, EntityPlayer player, RayTraceResult rayTraceResult, EnumHand hand) {
		if (wrench == null || wrench.isEmpty() || player == null || rayTraceResult == null || hand == null) {
			return null;
		}
		for (IWrenchHandler handler : handlers) {
			if(handler.canWrench(wrench, player, rayTraceResult, hand))
				return handler;
		}
		return null;
	}
}
