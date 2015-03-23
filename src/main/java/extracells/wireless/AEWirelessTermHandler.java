package extracells.wireless;

import java.util.HashMap;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;
import extracells.api.IWirelessFluidTermHandler;

public class AEWirelessTermHandler implements IWirelessTermHandler {

	// Only added to avoid crashes
	private class ConfigManager implements IConfigManager {

		HashMap<Enum, Enum> settings = new HashMap<Enum, Enum>();

		@Override
		public Enum getSetting(Enum settingName) {
			return this.settings.get(settingName);
		}

		@Override
		public Set<Enum> getSettings() {
			return this.settings.keySet();
		}

		@Override
		public Enum putSetting(Enum settingName, Enum newValue) {
			this.settings.put(settingName, newValue);
			return newValue;
		}

		@Override
		public void readFromNBT(NBTTagCompound src) {

		}

		@Override
		public void registerSetting(Enum settingName, Enum defaultValue) {
			this.settings.put(settingName, defaultValue);
		}

		@Override
		public void writeToNBT(NBTTagCompound dest) {

		}

	}

	@Override
	public boolean canHandle(ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry
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
		IWirelessFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(item);
		if (handler == null)
			return null;
		return handler.getEncryptionKey(item);
	}

	@Override
	public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(is);
		if (handler == null)
			return false;
		return handler.hasPower(player, amount, is);
	}

	@Override
	public void setEncryptionKey(ItemStack item, String encKey, String name) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(item);
		if (handler == null)
			return;
		handler.setEncryptionKey(item, encKey, name);
	}

	@Override
	public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry
				.getWirelessTermHandler(is);
		if (handler == null)
			return false;
		return handler.usePower(player, amount, is);
	}

}
