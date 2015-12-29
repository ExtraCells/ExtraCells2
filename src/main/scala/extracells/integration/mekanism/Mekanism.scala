package extracells.integration.mekanism

import extracells.integration.Integration.Mods


object Mekanism {

  def init: Unit ={
    if(Mods.MEKANISMGAS.isEnabled){
      Class.forName("")
    }
  }
}
