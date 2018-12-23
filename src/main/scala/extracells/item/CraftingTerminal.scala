package extracells.item

import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fml.common.Optional
import p455w0rd.wct.api.IWirelessCraftingTerminalItem

@Optional.Interface(iface = "p455w0rd.wct.api.IWirelessCraftingTerminalItem", modid = "wct", striprefs = true)
trait CraftingTerminal extends Item with IWirelessCraftingTerminalItem {

  //@Optional.Method(modid = "wct")
  //override def isWirelessCraftingEnabled(itemStack: ItemStack): Boolean = {
  //  if (this == ItemWirelessTerminalUniversal)
  //    ItemWirelessTerminalUniversal.isInstalled(itemStack, WirelessTerminalType.CRAFTING)
  //  else
  //    true
  //}
}
