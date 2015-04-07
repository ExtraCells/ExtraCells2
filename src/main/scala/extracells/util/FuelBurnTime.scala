package extracells.util


import buildcraft.api.fuels.BuildcraftFuelRegistry
import cpw.mods.fml.common.Optional
import extracells.integration.Integration
import net.minecraftforge.fluids.Fluid

import scala.collection.mutable

object FuelBurnTime {
  val fluidBurnTimes = new mutable.HashMap[Fluid, Integer]

  def registerFuel(fluid: Fluid, burnTime: Int): Unit ={
    if(!fluidBurnTimes.contains(fluid))
      fluidBurnTimes.put(fluid, burnTime)

  }

  def getBurnTime(fluid: Fluid): Int ={
    if(fluid == null)
      return 0
    if(fluidBurnTimes.contains(fluid))
      return fluidBurnTimes.get(fluid).get
    if(Integration.Mods.BCFUEL.isEnabled)
      return getBCBurnTime(fluid)
    0
  }

  @Optional.Method(modid = "BuildCraftAPI|fuels")
  private def getBCBurnTime(fluid: Fluid): Int = {
    val it = BuildcraftFuelRegistry.fuel.getFuels.iterator()
    while (it.hasNext){
      val fuel = it.next
      if(fuel.getFluid == fluid)
        return fuel.getTotalBurningTime
    }
    0
  }


}
