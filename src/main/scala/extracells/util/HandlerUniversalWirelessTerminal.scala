package extracells.util

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import extracells.api.{IWirelessGasTermHandler, IWirelessFluidTermHandler}
import extracells.item.ItemWirelessTerminalUniversal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack


object HandlerUniversalWirelessTerminal extends IWirelessTermHandler with IWirelessFluidTermHandler with IWirelessGasTermHandler{
  override def getConfigManager(is: ItemStack): IConfigManager = ItemWirelessTerminalUniversal.getConfigManager(is)

  override def canHandle(is: ItemStack): Boolean = ItemWirelessTerminalUniversal.canHandle(is)

  override def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = ItemWirelessTerminalUniversal.usePower(player, amount, is)

  override def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = ItemWirelessTerminalUniversal.hasPower(player, amount, is)

  override def isItemNormalWirelessTermToo(is: ItemStack): Boolean = ItemWirelessTerminalUniversal.isItemNormalWirelessTermToo(is)

  override def setEncryptionKey(item: ItemStack, encKey: String, name: String): Unit = ItemWirelessTerminalUniversal.setEncryptionKey(item, encKey, name)

  override def getEncryptionKey(item: ItemStack): String = ItemWirelessTerminalUniversal.getEncryptionKey(item)
}
