package extracells.integration.WirelessCrafting

import net.minecraft.entity.player.EntityPlayer
import p455w0rd.wct.api.WCTApi





object WirelessCrafting {
  def openCraftingTerminal(player: EntityPlayer) =

  WCTApi.instance.interact.openWirelessCraftingTerminalGui(player)

  def getBoosterItem =

  WCTApi.instance.items.infinityBoosterCard.getItem

  def isBoosterEnabled =

  WCTApi.isInfinityBoosterCardEnabled

  def getCraftingTerminal =

  WCTApi.instance.items.wirelessCraftingTerminal.getStack

}
