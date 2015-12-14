package extracells.wireless;

import extracells.api.IWirelessGasFluidTermHandler;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class WirelessTermRegistry {

	public static IWirelessGasFluidTermHandler getWirelessTermHandler(ItemStack is) {
		if (is == null)
			return null;
		for (IWirelessGasFluidTermHandler handler : handlers) {
			if (handler.canHandle(is))
				return handler;
		}
		return null;
	}

	public static boolean isWirelessItem(ItemStack is) {
		if (is == null)
			return false;
		for (IWirelessGasFluidTermHandler handler : handlers) {
			if (handler.canHandle(is))
				return true;
		}
		return false;
	}

	public static void registerWirelessTermHandler(
			IWirelessGasFluidTermHandler handler) {
		if (!handlers.contains(handler))
			handlers.add(handler);
	}

	static List<IWirelessGasFluidTermHandler> handlers = new ArrayList<IWirelessGasFluidTermHandler>();

}
