package extracells.integration.thaumaticenergistics

import net.minecraft.entity.player.EntityPlayer
import thaumicenergistics.api.{IThEWirelessEssentiaTerminal, ThEApi}

/**
  * Created by mjeli on 29.11.2015.
  */
object ThaumaticEnergistics {

  def openEssentiaTerminal(player :EntityPlayer, terminal :Any) :Unit =
    ThEApi.instance.interact.openWirelessTerminalGui(player, terminal.asInstanceOf[IThEWirelessEssentiaTerminal])

}
