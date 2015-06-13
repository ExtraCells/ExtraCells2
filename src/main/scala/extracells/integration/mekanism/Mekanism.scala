package extracells.integration.mekanism

import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.api.ECApi
import mekanism.api.gas.{GasRegistry, Gas}
import net.minecraft.util.IIcon
import net.minecraftforge.fluids.{FluidStack, Fluid}
import scala.collection.JavaConversions._


object Mekanism {

  private var fluidGas:Map[Gas, Fluid] = Map()

  def init {

  }

  def getFluidGasMap  = mapAsJavaMap(fluidGas)

  def postInit{
    val it = GasRegistry.getRegisteredGasses.iterator
    while(it.hasNext){
      val g = it.next
      val fluid = new GasFluid(g)
      fluidGas += (g -> fluid)
    }
    //ECApi.instance.addFluidToShowBlacklist(classOf[GasFluid])
    ECApi.instance.addFluidToStorageBlacklist(classOf[GasFluid])
  }

  class GasFluid(gas: Gas) extends Fluid("ec.internal." + gas.getName){
    override def getLocalizedName (stack: FluidStack) =  gas.getLocalizedName

    override def getIcon = gas.getIcon

    override def getStillIcon = gas.getIcon

    override def getFlowingIcon = gas.getIcon

    def getGas = gas
  }

}
