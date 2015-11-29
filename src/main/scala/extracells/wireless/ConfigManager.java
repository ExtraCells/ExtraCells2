package extracells.wireless;


import appeng.api.config.Settings;
import appeng.api.util.IConfigManager;
import net.minecraft.nbt.NBTTagCompound;

import java.util.HashMap;
import java.util.Set;

public class ConfigManager implements IConfigManager {

    HashMap<Settings, Enum> settings = new HashMap<Settings, Enum>();

    @Override
    public Enum<?> getSetting(Settings settingName) {
        return this.settings.get(settingName);
    }

    @Override
    public Set<Settings> getSettings() {
        return this.settings.keySet();
    }

    @Override
    public Enum<?> putSetting(Settings settingName, Enum<?> newValue) {
        this.settings.put(settingName, newValue);
        return newValue;
    }

    @Override
    public void readFromNBT(NBTTagCompound src) {

    }

    @Override
    public void registerSetting(Settings settingName, Enum defaultValue) {
        this.settings.put(settingName, defaultValue);
    }

    @Override
    public void writeToNBT(NBTTagCompound dest) {

    }

}