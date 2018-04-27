package extracells.item

import extracells.integration.wct.WirelessCrafting
import net.minecraft.item.{Item, ItemStack}
import net.minecraftforge.fml.common.Optional
import p455w0rd.wct.api.IWirelessCraftingTerminalItem

@Optional.Interface(iface = "p455w0rd.wct.api.IWirelessCraftingTerminalItem", modid = "wct", striprefs = true)
trait CraftingTerminal extends Item with IWirelessCraftingTerminalItem {
/*
  @Optional.Method(modid = "wct")
  override def checkForBooster(wirelessTerminal: ItemStack): Boolean = {
    if (wirelessTerminal.hasTagCompound()) {
      val boosterNBTList = wirelessTerminal.getTagCompound().getTagList("BoosterSlot", 10)
      if (boosterNBTList != null) {
        val boosterTagCompound = boosterNBTList.getCompoundTagAt(0)
        if (boosterTagCompound != null) {
          val boosterCard = new ItemStack(boosterTagCompound)
          if (boosterCard != null) {
            return boosterCard.getItem() == WirelessCrafting.getBoosterItem && WirelessCrafting.isBoosterEnabled
          }
        }
      }
    }

    false
  }*/

  @Optional.Method(modid = "wct")
  override def isWirelessCraftingEnabled(itemStack: ItemStack): Boolean = {
    if (this == ItemWirelessTerminalUniversal)
      ItemWirelessTerminalUniversal.isInstalled(itemStack, WirelessTerminalType.CRAFTING)
    else
      true
  }
}
