package extracells.part.gas

import appeng.api.parts.IPartCollisionHelper
import appeng.api.util.{AECableType, AEPartLocation}
import extracells.integration.mekanism.gas.GasInterfaceBase
import extracells.part.PartECBase
import mekanism.api.gas.GasTank
import net.minecraft.util.EnumFacing
import net.minecraftforge.fml.common.Optional.Method


class PartGasInterface extends PartECBase with GasInterfaceBase{
  var fluidFilter = ""

  override def getCableConnectionLength(aeCableType: AECableType): Float = ???

  /*override def renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper, renderer: RenderBlocks): Unit = ???

  override def renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks): Unit = ???*/

  override def getBoxes(bch: IPartCollisionHelper): Unit = ???

  @Method(modid = "MekanismAPI|gas")
  override def getGasTank(side: EnumFacing): GasTank = ???

  override def getFilter(side: AEPartLocation): String = fluidFilter

  override def setFilter(side: AEPartLocation, fluid: String): Unit = fluidFilter = fluid
}
