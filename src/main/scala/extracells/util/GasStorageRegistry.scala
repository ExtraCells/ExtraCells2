package extracells.util

import appeng.api.networking.security.BaseActionSource
import extracells.api.IExternalGasStorageHandler
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing

import scala.collection.mutable


object GasStorageRegistry {

  private val handler = mutable.MutableList[IExternalGasStorageHandler]()

  def addExternalStorageInterface(esh: IExternalGasStorageHandler): Unit = {
    handler += esh
  }

  def getHandler(te: TileEntity, opposite: EnumFacing, mySrc: BaseActionSource): IExternalGasStorageHandler = {
    for(x <- handler){
      if(x.canHandle(te, opposite, mySrc))
        return x
    }
    null
  }

}
