package extracells.integration.opencomputers

import appeng.api.AEApi
import extracells.integration.Integration
import extracells.item.ItemOCUpgrade
import li.cil.oc.api.Driver
import li.cil.oc.api.driver._
import li.cil.oc.api.prefab.{DriverBlock, DriverItem}

object OpenComputers {

	def init{
		add(new DriverFluidExportBus)
		add(new DriverFluidImportBus)
		add(new DriverOreDictExportBus)
		add(new DriverFluidInterface)
		if(Integration.Mods.MEKANISMGAS.isEnabled){
			add(new DriverGasExportBus)
			add(new DriverGasImportBus)
		}
		add(ItemOCUpgrade)
		OCRecipes.loadRecipes()
		AEApi.instance.registries.wireless.registerWirelessHandler(WirelessHandlerUpgradeAE)
		ExtraCellsPathProvider
	}

	def add(provider: AnyRef): Unit ={
		if (provider.isInstanceOf[EnvironmentProvider]) Driver.add(provider.asInstanceOf[EnvironmentProvider])
		if (provider.isInstanceOf[DriverItem]) Driver.add(provider.asInstanceOf[DriverItem])
		if (provider.isInstanceOf[DriverBlock]) Driver.add(provider.asInstanceOf[DriverBlock])
		if (provider.isInstanceOf[SidedBlock]) Driver.add(provider.asInstanceOf[SidedBlock])
		if (provider.isInstanceOf[Converter]) Driver.add(provider.asInstanceOf[Converter])
		if (provider.isInstanceOf[InventoryProvider]) Driver.add(provider.asInstanceOf[InventoryProvider])
	}

}
