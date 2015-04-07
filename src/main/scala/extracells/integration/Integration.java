package extracells.integration;

import net.minecraftforge.common.config.Configuration;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModAPIManager;
import extracells.integration.opencomputers.OpenComputers;
import extracells.integration.waila.Waila;

public class Integration {
	
	public enum Mods{
		WAILA("Waila"),
		OPENCOMPUTERS("OpenComputers"),
		BCFUEL("BuildCraftAPI|fuels", "BuildCraftFuel");
		
		private final String modID;
		
		private boolean shouldLoad = true;
		
		private final String name;

		private Mods(String modid){
			this(modid, modid);
		}

		private Mods(String modid, String modName) {
			this.modID = modid;
			this.name = modName;
		}
		
		public String getModID(){
			return modID;
		}

		public String getModName() {
			return name;
		}
		
		public void loadConfig(Configuration config){
			shouldLoad = config.get("Integration", "enable" + getModName(), true, "Enable " + getModName() + " Integration.").getBoolean(true);
		}
		
		public boolean isEnabled(){
			return (Loader.isModLoaded(getModID()) && shouldLoad) || (ModAPIManager.INSTANCE.hasAPI(getModID()) && shouldLoad);
		}
		
		
	}
	
	
	public void loadConfig(Configuration config){
		for (Mods mod : Mods.values()){
			mod.loadConfig(config);
		}
	}
	
	
	public void preInit(){
		
	}
	
	public void init(){
		if (Mods.WAILA.isEnabled())
			Waila.init();
		if (Mods.OPENCOMPUTERS.isEnabled())
			OpenComputers.init();
	}
	
	public void postInit(){
		
	}

}
