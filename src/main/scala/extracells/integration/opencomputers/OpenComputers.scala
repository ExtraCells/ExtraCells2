package extracells.integration.opencomputers

import appeng.api.AEApi
import extracells.integration.Integration
import extracells.item.ItemOCUpgrade
import li.cil.oc.api.Driver
import li.cil.oc.api.driver.EnvironmentProvider
import net.minecraftforge.common.config.Configuration

object OpenComputers {

	def init{
		add(new DriverFluidExportBus)
		add(new DriverOreDictExportBus)
		add(new DriverFluidInterface)
		if(Integration.Mods.MEKANISMGAS.isEnabled){
			add(new DriverGasExportBus)
			add(new DriverGasImportBus)
		}
		Driver.add(ItemOCUpgrade)
		AEApi.instance.registries.wireless.registerWirelessHandler(WirelessHandlerUpgradeAE)
		//OCRecipes.loadRecipes
		ExtraCellsPathProvider
	}

	def add(provider: EnvironmentProvider): Unit ={
		Driver.add(provider)
	}

}
