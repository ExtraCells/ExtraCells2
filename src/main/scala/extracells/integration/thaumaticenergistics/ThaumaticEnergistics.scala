package extracells.integration.thaumaticenergistics

import net.minecraft.entity.player.EntityPlayer
import thaumicenergistics.api.{IThEWirelessEssentiaTerminal, ThEApi}


object ThaumaticEnergistics {

  def openEssentiaTerminal(player :EntityPlayer, terminal :Any) :Unit =
    ThEApi.instance.interact.openWirelessTerminalGui(player, terminal.asInstanceOf[IThEWirelessEssentiaTerminal])

  def getTerminal = ThEApi.instance.parts.Essentia_Terminal.getStack

  def getWirelessTerminal = ThEApi.instance.items.WirelessEssentiaTerminal.getStack

}
