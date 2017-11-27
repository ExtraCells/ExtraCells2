package extracells.integration.cofh.item

import extracells.api.ECApi


object CofhItem {

  def init: Unit = {
    ECApi.instance.registerWrenchHandler(WrenchHandler)
  }

}
