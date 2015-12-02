package extracells.integration.opencomputers

import appeng.api.AEApi
import extracells.integration.Integration
import li.cil.oc.api.Driver
object OpenComputers {
	
	def init{
		Driver.add(new DriverFluidExportBus)
		Driver.add(new DriverOreDictExportBus)
		Driver.add(new DriverFluidInterface)
		if(Integration.Mods.MEKANISMGAS.isEnabled){
			Driver.add(new DriverGasExportBus)
			Driver.add(new DriverGasImportBus)
		}
		Driver.add(ItemUpgradeAE)
		AEApi.instance.registries.wireless.registerWirelessHandler(WirelessHandlerUpgradeAE)
		OCRecipes.loadRecipes
		ExtraCellsPathProvider
	}

}
