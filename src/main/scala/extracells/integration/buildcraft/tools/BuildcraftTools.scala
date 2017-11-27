package extracells.integration.buildcraft.tools

import extracells.api.ECApi


object BuildcraftTools {

  def init: Unit = {
    ECApi.instance.registerWrenchHandler(WrenchHandler)
  }

}
