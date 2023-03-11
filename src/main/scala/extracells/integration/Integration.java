package extracells.integration;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import cpw.mods.fml.relauncher.Side;
import extracells.Extracells;
import extracells.integration.igw.IGW;
import extracells.integration.mekanism.Mekanism;
import extracells.integration.mekanism.gas.MekanismGas;
import extracells.integration.nei.Nei;
import extracells.integration.opencomputers.OpenComputers;
import extracells.integration.waila.Waila;

public class Integration {

    public enum Mods {

        WAILA("Waila"),
        OPENCOMPUTERS("OpenComputers"),
        BCFUEL("BuildCraftAPI|fuels", "BuildCraftFuel"),
        NEI("NotEnoughItems", Side.CLIENT),
        MEKANISMGAS("MekanismAPI|gas", "MekanismGas"),
        IGW("IGWMod", "IngameWikiMod", Side.CLIENT),
        THAUMATICENERGISTICS("thaumicenergistics", "Thaumatic Energistics"),
        MEKANISM("Mekanism"),
        WIRELESSCRAFTING("ae2wct", "AE2 Wireless Crafting Terminal"),
        FLUIDCRAFT("ae2fc", "Fluid Craft For AE2");

        private final String modID;

        private boolean shouldLoad = true;

        private final String name;

        private final Side side;

        private Mods(String modid) {
            this(modid, modid);
        }

        private Mods(String modid, String modName, Side side) {
            this.modID = modid;
            this.name = modName;
            this.side = side;
            this.enabled = (Loader.isModLoaded(getModID()) && shouldLoad && correctSide())
                    || (ModAPIManager.INSTANCE.hasAPI(getModID()) && shouldLoad && correctSide());
        }

        private Mods(String modid, String modName) {
            this(modid, modName, null);
        }

        private Mods(String modid, Side side) {
            this(modid, modid, side);
        }

        public String getModID() {
            return modID;
        }

        public String getModName() {
            return name;
        }

        public boolean isOnClient() {
            return side != Side.SERVER;
        }

        public boolean isOnServer() {
            return side != Side.CLIENT;
        }

        private final boolean enabled;

        public void loadConfig(Configuration config) {
            shouldLoad = config
                    .get("Integration", "enable" + getModName(), true, "Enable " + getModName() + " Integration.")
                    .getBoolean(true);
        }

        public boolean isEnabled() {
            return enabled;
        }

        private boolean correctSide() {
            return Extracells.proxy().isClient() ? isOnClient() : isOnServer();
        }
    }

    public void loadConfig(Configuration config) {
        for (Mods mod : Mods.values()) {
            mod.loadConfig(config);
        }
    }

    public void preInit() {
        if (Mods.IGW.correctSide() && Mods.IGW.shouldLoad) IGW.initNotifier();
    }

    public void init() {
        if (Mods.WAILA.isEnabled()) Waila.init();
        if (Mods.OPENCOMPUTERS.isEnabled()) OpenComputers.init();
        if (Mods.NEI.isEnabled()) Nei.init();
        if (Mods.MEKANISMGAS.isEnabled()) MekanismGas.init();
        if (Mods.IGW.isEnabled()) IGW.init();
        if (Mods.MEKANISM.isEnabled()) Mekanism.init();
    }

    public void postInit() {
        if (Mods.MEKANISMGAS.isEnabled()) MekanismGas.postInit();
    }
}
