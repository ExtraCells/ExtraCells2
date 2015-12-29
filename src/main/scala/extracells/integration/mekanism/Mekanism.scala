package extracells.integration.mekanism

import extracells.api.ECApi
import extracells.integration.Integration.Mods


object Mekanism {

  def init: Unit ={
    if(Mods.MEKANISMGAS.isEnabled){
      ECApi.instance().addExternalStorageInterface(HandlerMekanismGasTank)
    }
  }
}
