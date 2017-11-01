package extracells.integration.jei

import mezz.jei.api.IModRegistry
import net.minecraftforge.fluids.FluidStack

import scala.collection.mutable.ListBuffer


object Jei {

  val fluidBlacklist = new ListBuffer[FluidStack]

  var registry: IModRegistry = null;

  def addFluidToBlacklist(stack : FluidStack): Unit ={
    if (registry != null){
      registry.getJeiHelpers.getIngredientBlacklist.addIngredientToBlacklist(stack)
    }else{
      fluidBlacklist += stack
    }
  }

}
