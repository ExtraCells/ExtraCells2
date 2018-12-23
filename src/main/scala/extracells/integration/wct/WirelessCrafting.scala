package extracells.integration.wct

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import p455w0rd.ae2wtlib.api.WTApi
import p455w0rd.wct.api.WCTApi


object WirelessCrafting {
  def openCraftingTerminal(player: EntityPlayer, slot: Int) = WCTApi.instance.openWCTGui(player, false, slot)

  def getBoosterItem = new ItemStack(Item.getByNameOrId("wct:infinity_booster_card"))

  def isBoosterEnabled = WTApi.instance().getConfig.isInfinityBoosterCardEnabled

  def getCraftingTerminal = new ItemStack(Item.getByNameOrId("wct:wct"))

}
