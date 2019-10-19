package extracells.util

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import extracells.api.{IWirelessFluidTermHandler, IWirelessGasTermHandler}
import extracells.item.ItemWirelessTerminalUniversal
import extracells.registries.ItemEnum
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack


object HandlerUniversalWirelessTerminal extends IWirelessTermHandler with IWirelessFluidTermHandler with IWirelessGasTermHandler {
  private def terminal = ItemEnum.UNIVERSALTERMINAL.getItem.asInstanceOf[ItemWirelessTerminalUniversal]

  override def getConfigManager(is: ItemStack): IConfigManager = terminal.getConfigManager(is)

  override def canHandle(is: ItemStack): Boolean = terminal.canHandle(is)

  override def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = terminal.usePower(player, amount, is)

  override def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = terminal.hasPower(player, amount, is)

  override def isItemNormalWirelessTermToo(is: ItemStack): Boolean = terminal.isItemNormalWirelessTermToo(is)

  override def setEncryptionKey(item: ItemStack, encKey: String, name: String): Unit = terminal.setEncryptionKey(item, encKey, name)

  override def getEncryptionKey(item: ItemStack): String = terminal.getEncryptionKey(item)
}
