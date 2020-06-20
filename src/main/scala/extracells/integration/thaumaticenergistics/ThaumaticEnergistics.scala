package extracells.integration.thaumaticenergistics

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import thaumicenergistics.api.ThEApi


object ThaumaticEnergistics {

  def openEssentiaTerminal(player :EntityPlayer, terminal :Any) :Unit =
    ThEApi.instance.interact.openWirelessTerminalGui(player)

  def getTerminal: ItemStack = ThEApi.instance.parts.Essentia_Terminal.getStack

  def getWirelessTerminal: ItemStack = ThEApi.instance.items.WirelessEssentiaTerminal.getStack

}
