package extracells.integration.WirelessCrafting

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import p455w0rd.wct.api.WCTApi
//import p455w0rd.wct.api.WCTApi





object WirelessCrafting {
  def openCraftingTerminal(player: EntityPlayer) = WCTApi.instance.openWirelessCraftingTerminalGui(player)

  def getBoosterItem = new ItemStack(Item.getByNameOrId("wct:infinity_booster_card"))

  def isBoosterEnabled = WCTApi.instance().isInfinityBoosterCardEnabled

  def getCraftingTerminal = new ItemStack(Item.getByNameOrId("wct:wct"))

}
