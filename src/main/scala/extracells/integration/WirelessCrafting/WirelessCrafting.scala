package extracells.integration.WirelessCrafting

import net.minecraft.entity.player.EntityPlayer
import net.p455w0rd.wirelesscraftingterminal.api.WCTApi
import net.p455w0rd.wirelesscraftingterminal.reference.Reference;


object WirelessCrafting {
  def openCraftingTerminal(player :EntityPlayer) :Unit =
    WCTApi.instance.interact.openWirelessCraftingTerminalGui(player)

  def getBoosterItem =
    WCTApi.instance.items.InfinityBoosterCard.getItem

  def isBoosterEnabled =
    Reference.WCT_BOOSTER_ENABLED

  def getCraftingTerminal =
    WCTApi.instance.items.WirelessCraftingTerminal.getStack

}
