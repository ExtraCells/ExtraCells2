package extracells.integration;

import cpw.mods.fml.common.Loader;
import extracells.integration.opencomputers.OpenComputers;
import extracells.integration.waila.Waila;

public class Intigration {
	
	public void preInit(){
		
	}
	
	public void init(){
		if (Loader.isModLoaded("Waila"))
			Waila.init();
		if (Loader.isModLoaded("OpenComputers"))
			OpenComputers.init();
	}
	
	public void postInit(){
		
	}

}
