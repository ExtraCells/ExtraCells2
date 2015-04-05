package extracells.wireless;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import extracells.api.IWirelessFluidTermHandler;

public class WirelessTermRegistry {

	public static IWirelessFluidTermHandler getWirelessTermHandler(ItemStack is) {
		if (is == null)
			return null;
		for (IWirelessFluidTermHandler handler : handlers) {
			if (handler.canHandle(is))
				return handler;
		}
		return null;
	}

	public static boolean isWirelessItem(ItemStack is) {
		if (is == null)
			return false;
		for (IWirelessFluidTermHandler handler : handlers) {
			if (handler.canHandle(is))
				return true;
		}
		return false;
	}

	public static void registerWirelessFluidTermHandler(
			IWirelessFluidTermHandler handler) {
		if (!handlers.contains(handler))
			handlers.add(handler);
	}

	static List<IWirelessFluidTermHandler> handlers = new ArrayList<IWirelessFluidTermHandler>();

}
