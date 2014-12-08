package extracells.wireless;

import java.util.HashMap;
import java.util.Set;

import extracells.api.IWirelessFluidTermHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.util.IConfigManager;

public class AEWirelessTermHandler implements IWirelessTermHandler {
	
	@Override
	public boolean canHandle(ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(is);
		if(handler == null)
			return false;
		return !handler.isItemNormalWirelessTermToo(is);
	}

	@Override
	public String getEncryptionKey(ItemStack item) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(item);
		if(handler == null)
			return null;
		return handler.getEncryptionKey(item);
	}

	@Override
	public void setEncryptionKey(ItemStack item, String encKey, String name) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(item);
		if(handler == null)
			return;
		handler.setEncryptionKey(item, encKey, name);
	}

	@Override
	public boolean usePower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(is);
		if(handler == null)
			return false;
		return handler.usePower(player, amount, is);
	}

	@Override
	public boolean hasPower(EntityPlayer player, double amount, ItemStack is) {
		IWirelessFluidTermHandler handler = WirelessTermRegistry.getWirelessTermHandler(is);
		if(handler == null)
			return false;
		return handler.hasPower(player, amount, is);
	}

	@Override
	public IConfigManager getConfigManager(ItemStack is) {
		return new ConfigManager();
	}
	
	
	//Only added to avoid crashes
	private class ConfigManager implements IConfigManager{

		HashMap<Enum, Enum> settings = new HashMap<Enum, Enum>();
		
		@Override
		public Set<Enum> getSettings() {
			return settings.keySet();
		}

		@Override
		public void registerSetting(Enum settingName, Enum defaultValue) {
			settings.put(settingName, defaultValue);
		}

		@Override
		public Enum getSetting(Enum settingName) {
			return settings.get(settingName);
		}

		@Override
		public Enum putSetting(Enum settingName, Enum newValue) {
			settings.put(settingName, newValue);
			return newValue;
		}

		@Override
		public void writeToNBT(NBTTagCompound dest) {
			
		}

		@Override
		public void readFromNBT(NBTTagCompound src) {
			
		}
		
	}

}
