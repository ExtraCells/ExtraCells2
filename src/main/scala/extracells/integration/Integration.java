package extracells.integration;

import extracells.integration.appeng.AppEng;
import extracells.integration.buildcraft.tools.BuildcraftTools;
import extracells.integration.cofh.item.CofhItem;
import extracells.integration.igw.IGW;
import extracells.integration.opencomputers.OpenComputers;
import extracells.util.Log;
import net.minecraftforge.common.config.Configuration;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModAPIManager;
import net.minecraftforge.fml.relauncher.Side;

import extracells.ExtraCells;
import extracells.integration.mekanism.Mekanism;
import extracells.integration.mekanism.gas.MekanismGas;
import extracells.integration.waila.Waila;

public class Integration {

	public enum Mods {
		WAILA("waila"),
		OPENCOMPUTERS("opencomputers"),
		BCFUEL("BuildCraftAPI|fuels", "BuildCraftFuel"),
		MEKANISMGAS("MekanismAPI|gas", "MekanismGas"),
		IGW("igwmod", "IngameWikiMod", Side.CLIENT),
		THAUMATICENERGISTICS("thaumicenergistics", "Thaumatic Energistics"),
		MEKANISM("mekanism"),
		WIRELESSCRAFTING("wct", "AE2 Wireless Crafting Terminal"),
		JEI("jei", "Just Enough Items", Side.CLIENT),
		BUILDCRAFTTOOLS("BuildCraftAPI|tools", "BuildCraft Wrench"),
		COFHITEM("cofhapi|item", "COFH Hammer");

		private final String modID;

		private boolean shouldLoad = true;

		private final String name;

		private final Side side;

		Mods(String modid) {
			this(modid, modid);
		}

		Mods(String modid, String modName, Side side) {
			this.modID = modid;
			this.name = modName;
			this.side = side;
		}

		Mods(String modid, String modName) {
			this(modid, modName, null);
		}

		Mods(String modid, Side side) {
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

		public void loadConfig(Configuration config) {
			shouldLoad = config.get("Integration", "enable" + getModID(), true, "Enable " + getModName() + " Integration.").getBoolean(true);
		}

		public boolean isEnabled() {
			return (Loader.isModLoaded(getModID()) && shouldLoad && correctSide()) || (ModAPIManager.INSTANCE.hasAPI(getModID()) && shouldLoad && correctSide());
		}

		private boolean correctSide() {
			return ExtraCells.proxy.isClient() ? isOnClient() : isOnServer();
		}
	}

	public void loadConfig(Configuration config) {
		for (Mods mod : Mods.values()) {
			mod.loadConfig(config);
		}
	}


	public void preInit() {
		for(Mods mod : Mods.values()){
			if(mod.isEnabled())
				Log.info("Enable integration for '" + mod.name + " (" + mod.modID + ")'");
		}

		if (Mods.IGW.correctSide() && Mods.IGW.shouldLoad)
			IGW.initNotifier();
		if (Mods.MEKANISMGAS.isEnabled())
			MekanismGas.preInit();
	}

	public void init() {
		if (Mods.WAILA.isEnabled())
			Waila.init();
		if (Mods.OPENCOMPUTERS.isEnabled())
			OpenComputers.init();
		if (Mods.MEKANISMGAS.isEnabled())
			MekanismGas.init();
		if (Mods.IGW.isEnabled())
			IGW.init();
		if (Mods.MEKANISM.isEnabled())
			Mekanism.init();
		if (Mods.BUILDCRAFTTOOLS.isEnabled())
			BuildcraftTools.init();
		if (Mods.COFHITEM.isEnabled())
			CofhItem.init();

		AppEng.init();
	}

	public void postInit() {
		if (Mods.MEKANISMGAS.isEnabled()) {
			MekanismGas.postInit();
		}
	}

}
