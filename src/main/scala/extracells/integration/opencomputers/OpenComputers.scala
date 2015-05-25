package extracells.integration.opencomputers

import appeng.api.AEApi
import li.cil.oc.api.Driver
object OpenComputers {
	
	def init{
		Driver.add(new DriverFluidExportBus())
		Driver.add(new DriverOreDictExportBus())
		Driver.add(new DriverFluidInterface())
		Driver.add(ItemUpgradeAE)
		AEApi.instance.registries.wireless.registerWirelessHandler(WirelessHandlerUpgradeAE)
		OCRecipes.loadRecipes

	}

}
