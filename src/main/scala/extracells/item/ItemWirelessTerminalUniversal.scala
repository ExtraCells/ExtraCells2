package extracells.item


import java.util

import appeng.api.AEApi
import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.api.{ECApi, IWirelessFluidTermHandler}
import extracells.integration.Integration
import extracells.integration.thaumaticenergistics.ThaumaticEnergistics
import extracells.wireless.{ConfigManager, AEWirelessTermHandler}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{StatCollector, IIcon}
import net.minecraft.world.World

object ItemWirelessTerminalUniversal extends ItemECBase with WirelessTermBase with IWirelessFluidTermHandler with IWirelessTermHandler with EssensiaTerminal{
  val isTeEnabled = Integration.Mods.THAUMATICENERGISTICS.isEnabled
  val isMekEnabled = Integration.Mods.MEKANISMGAS.isEnabled
  override val MAX_POWER: Double = 3200000
  var icon :IIcon = null
  def THIS = this
  ECApi.instance.registerWirelessFluidTermHandler(this)
  AEApi.instance.registries.wireless.registerWirelessHandler(this)



  override def isItemNormalWirelessTermToo(is: ItemStack): Boolean = true

  override def getConfigManager(itemStack: ItemStack): IConfigManager = {
    new ConfigManager()
  }

  private def ensureTagCompound(itemStack: ItemStack): NBTTagCompound = {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    return itemStack.getTagCompound
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")
  }

  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack = {
    if(world.isRemote)
      return itemStack
    val tag = ensureTagCompound(itemStack)
    if(!tag.hasKey("type"))
      tag.setByte("type", 0)
    if(entityPlayer.isSneaking)
      return changeMode(itemStack, entityPlayer, tag)
    val matchted = tag.getByte("type") match {
      case 0 => AEApi.instance.registries.wireless.openWirelessTerminalGui(itemStack, world, entityPlayer)
      case 1 => ECApi.instance().openWirelessTerminal(entityPlayer, itemStack, world)
      case 3 => if(isTeEnabled) ThaumaticEnergistics.openEssentiaTerminal(entityPlayer, this)
      case _ =>
    }
    return itemStack
  }

  def changeMode(itemStack: ItemStack, player: EntityPlayer, tag :NBTTagCompound): ItemStack ={
    val matchted = tag.getByte("type") match {
      case 0 => tag.setByte("type", 1)
      case 1 =>
        if(isMekEnabled)
          tag.setByte("type", 2)
        else if(isTeEnabled)
          tag.setByte("type", 3)
        else
          tag.setByte("type", 0)
      case 2 =>
        if(isTeEnabled)
          tag.setByte("type", 3)
        else
          tag.setByte("type", 0)
      case _ =>
          tag.setByte("type", 0)
    }
    return itemStack
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:" + "terminal.universal.wireless")
  }

  override def getIconFromDamage(dmg: Int): IIcon = {
    return this.icon
  }

  @SideOnly(Side.CLIENT)
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean) {
    val tag = ensureTagCompound(itemStack)
    if(!tag.hasKey("type"))
      tag.setByte("type", 0)
    val list2 = list.asInstanceOf[util.List[String]];
    list2.add(StatCollector.translateToLocal("extracells.tooltip.mode") + ": " + StatCollector.translateToLocal("extracells.tooltip." + TerminalType.values().apply(tag.getByte("type")).toString.toLowerCase))
    super.addInformation(itemStack, player, list, par4);
  }


}
