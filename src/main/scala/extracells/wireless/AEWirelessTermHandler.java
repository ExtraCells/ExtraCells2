package extracells.wireless;

import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import extracells.api.IWirelessGasFluidTermHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class AEWirelessTermHandler implements IWirelessTermHandler {

	@Override
	public boolean canHandle(ItemStack is) {
		IWirelessGasFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(is);
		if (handler == null)
			return false;
		return !handler.isItemNormalWirelessTermToo(is);
	}

	@Override
	public IConfigManager getConfigManager(ItemStack is) {
		return new ConfigManager();
	}

	@Override
	public String getEncryptionKey(ItemStack item) {
		IWirelessGasFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(item);
		if (handler == null)
			return null;
		return handler.getEncryptionKey(item);
	}

	@Override
	public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessGasFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(is);
		if (handler == null)
			return false;
		return handler.hasPower(player, amount, is);
	}

	@Override
	public void setEncryptionKey(ItemStack item, String encKey, String name) {
		IWirelessGasFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(item);
		if (handler == null)
			return;
		handler.setEncryptionKey(item, encKey, name);
	}

	@Override
	public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessGasFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(is);
		if (handler == null)
			return false;
		return handler.usePower(player, amount, is);
	}

}
