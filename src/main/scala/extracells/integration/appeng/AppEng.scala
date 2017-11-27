package extracells.integration.appeng

import extracells.api.ECApi

object AppEng {

  def init: Unit ={
    ECApi.instance.registerWrenchHandler(WrenchHandler)
  }

}
