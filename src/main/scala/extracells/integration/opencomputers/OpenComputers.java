package extracells.integration.opencomputers;

import li.cil.oc.api.Driver;

public class OpenComputers {
	
	public static void init(){
		Driver.add(new DriverFluidExportBus());
		Driver.add(new DriverOreDictExportBus());
		Driver.add(new DriverFluidInterface());
	}

}
