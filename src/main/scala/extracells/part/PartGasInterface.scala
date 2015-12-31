package extracells.part

import appeng.api.parts.{IPartCollisionHelper, IPartRenderHelper}
import cpw.mods.fml.common.Optional.Method
import extracells.integration.mekanism.gas.GasInterfaceBase
import mekanism.api.gas.GasTank
import net.minecraft.client.renderer.RenderBlocks
import net.minecraftforge.common.util.ForgeDirection


class PartGasInterface extends PartECBase with GasInterfaceBase{
  var fluidFilter = -1

  override def cableConnectionRenderTo(): Int = ???

  override def renderStatic(x: Int, y: Int, z: Int, rh: IPartRenderHelper, renderer: RenderBlocks): Unit = ???

  override def renderInventory(rh: IPartRenderHelper, renderer: RenderBlocks): Unit = ???

  override def getBoxes(bch: IPartCollisionHelper): Unit = ???

  @Method(modid = "MekanismAPI|gas")
  override def getGasTank(side: ForgeDirection): GasTank = ???

  override def getFilter(side: ForgeDirection): Int = fluidFilter

  override def setFilter(side: ForgeDirection, fluid: Int): Unit = fluidFilter = fluid
}
