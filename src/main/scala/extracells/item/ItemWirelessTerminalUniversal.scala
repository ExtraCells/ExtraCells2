package extracells.item


import java.util

import appeng.api.AEApi
import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.api.{ECApi, IWirelessFluidTermHandler, IWirelessGasTermHandler}
import extracells.integration.Integration
import extracells.integration.WirelessCrafting.WirelessCrafting
import extracells.integration.thaumaticenergistics.ThaumaticEnergistics
import extracells.util.HandlerUniversalWirelessTerminal
import extracells.wireless.ConfigManager
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{IIcon, StatCollector}
import net.minecraft.world.World

object ItemWirelessTerminalUniversal extends ItemECBase with WirelessTermBase with IWirelessFluidTermHandler with IWirelessGasTermHandler with IWirelessTermHandler with EssensiaTerminal with CraftingTerminal{
  val isTeEnabled = Integration.Mods.THAUMATICENERGISTICS.isEnabled
  val isMekEnabled = Integration.Mods.MEKANISMGAS.isEnabled
  val isWcEnabled = Integration.Mods.WIRELESSCRAFTING.isEnabled
  var icon :IIcon = null
  def THIS = this
  if(isWcEnabled){
    ECApi.instance.registerWirelessTermHandler(this)
    AEApi.instance.registries.wireless.registerWirelessHandler(this)
  }else{
    ECApi.instance.registerWirelessTermHandler(HandlerUniversalWirelessTerminal)
    AEApi.instance.registries.wireless.registerWirelessHandler(HandlerUniversalWirelessTerminal)
  }




  override def isItemNormalWirelessTermToo(is: ItemStack): Boolean = true

  override def getConfigManager(itemStack: ItemStack): IConfigManager = {
    val nbt = ensureTagCompound(itemStack)
    if(!nbt.hasKey("settings"))
      nbt.setTag("settings", new NBTTagCompound)
    val tag = nbt.getCompoundTag("settings")
    new ConfigManager(tag)
  }

  private def ensureTagCompound(itemStack: ItemStack): NBTTagCompound = {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    itemStack.getTagCompound
  }

  override def getUnlocalizedName(itemStack: ItemStack): String =
    super.getUnlocalizedName(itemStack).replace("item.extracells", "extracells.item")


  override def onItemRightClick(itemStack: ItemStack, world: World, entityPlayer: EntityPlayer): ItemStack = {
    if(world.isRemote){
      if(entityPlayer.isSneaking)
        return itemStack
      val tag = ensureTagCompound(itemStack)
      if(!tag.hasKey("type"))
        tag.setByte("type", 0)
      if(tag.getByte("type") == 4 && isWcEnabled)
        WirelessCrafting.openCraftingTerminal(entityPlayer)
      return itemStack
    }

    val tag = ensureTagCompound(itemStack)
    if(!tag.hasKey("type"))
      tag.setByte("type", 0)
    if(entityPlayer.isSneaking)
      return changeMode(itemStack, entityPlayer, tag)
    val matchted = tag.getByte("type") match {
      case 0 => AEApi.instance.registries.wireless.openWirelessTerminalGui(itemStack, world, entityPlayer)
      case 1 => ECApi.instance.openWirelessFluidTerminal(entityPlayer, itemStack, world)
      case 2 => ECApi.instance.openWirelessGasTerminal(entityPlayer, itemStack, world)
      case 3 => if(isTeEnabled) ThaumaticEnergistics.openEssentiaTerminal(entityPlayer, this)
      case _ =>
    }
    itemStack
  }

  def changeMode(itemStack: ItemStack, player: EntityPlayer, tag :NBTTagCompound): ItemStack = {
    val installed = getInstalledModules(itemStack)
    val matchted = tag.getByte("type") match {
      case 0 =>
        if(installed.contains(TerminalType.FLUID))
          tag.setByte("type", 1)
        else if(isMekEnabled && installed.contains(TerminalType.GAS))
          tag.setByte("type", 2)
        else if(isTeEnabled && installed.contains(TerminalType.ESSENTIA))
          tag.setByte("type", 3)
        else if(isWcEnabled && installed.contains(TerminalType.CRAFTING))
          tag.setByte("type", 4)
      case 1 =>
        if(isMekEnabled && installed.contains(TerminalType.GAS))
          tag.setByte("type", 2)
        else if(isTeEnabled && installed.contains(TerminalType.ESSENTIA))
          tag.setByte("type", 3)
        else if(isWcEnabled && installed.contains(TerminalType.CRAFTING))
          tag.setByte("type", 4)
        else if(installed.contains(TerminalType.ITEM))
          tag.setByte("type", 0)
      case 2 =>
        if(isTeEnabled && installed.contains(TerminalType.ESSENTIA))
          tag.setByte("type", 3)
        else if(isWcEnabled && installed.contains(TerminalType.CRAFTING))
          tag.setByte("type", 4)
        else if(installed.contains(TerminalType.ITEM))
          tag.setByte("type", 0)
        else if(installed.contains(TerminalType.FLUID))
          tag.setByte("type", 1)
      case 3 =>
        if(isWcEnabled && installed.contains(TerminalType.CRAFTING))
          tag.setByte("type", 4)
        else if(installed.contains(TerminalType.ITEM))
          tag.setByte("type", 0)
        else if(installed.contains(TerminalType.FLUID))
          tag.setByte("type", 1)
        else if(isMekEnabled && installed.contains(TerminalType.GAS))
          tag.setByte("type", 2)
      case _ =>
        if(installed.contains(TerminalType.ITEM))
          tag.setByte("type", 0)
        else if(installed.contains(TerminalType.FLUID))
          tag.setByte("type", 1)
        else if(isMekEnabled && installed.contains(TerminalType.GAS))
          tag.setByte("type", 2)
        else if(isTeEnabled && installed.contains(TerminalType.ESSENTIA))
          tag.setByte("type", 3)
        else if(isWcEnabled && installed.contains(TerminalType.CRAFTING))
          tag.setByte("type", 4)
        else
          tag.setByte("type", 0)
    }
    itemStack
  }

  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:" + "terminal.universal.wireless")
  }

  override def getIconFromDamage(dmg: Int): IIcon = this.icon


  @SideOnly(Side.CLIENT)
  override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean) {
    val tag = ensureTagCompound(itemStack)
    if(!tag.hasKey("type"))
      tag.setByte("type", 0)
    val list2 = list.asInstanceOf[util.List[String]];
    list2.add(StatCollector.translateToLocal("extracells.tooltip.mode") + ": " + StatCollector.translateToLocal("extracells.tooltip." + TerminalType.values().apply(tag.getByte("type")).toString.toLowerCase))
    list2.add(StatCollector.translateToLocal("extracells.tooltip.installed"))
    val it = getInstalledModules(itemStack).iterator
    while (it.hasNext)
      list2.add("- " + StatCollector.translateToLocal("extracells.tooltip." + it.next.name.toLowerCase))
    super.addInformation(itemStack, player, list, par4);
  }

  def installModule(itemStack: ItemStack, module: TerminalType): Unit ={
    if(isInstalled(itemStack, module))
      return
    val install = (1 << module.ordinal).toByte
    val tag = ensureTagCompound(itemStack)
    val installed: Byte = {
      if(tag.hasKey("modules"))
        (tag.getByte("modules") + install).toByte
      else
      install
    }
    tag.setByte("modules", installed)
  }

  def getInstalledModules(itemStack: ItemStack) :util.EnumSet[TerminalType] = {
    if(itemStack == null || itemStack.getItem == null)
      return util.EnumSet.noneOf(classOf[TerminalType])
    val tag = ensureTagCompound(itemStack)
    val installed: Byte = {
      if(tag.hasKey("modules"))
        tag.getByte("modules")
      else
        0
    }
    val set = util.EnumSet.noneOf(classOf[TerminalType])
    for(x <- TerminalType.values){
      if(1 == (installed >> x.ordinal) % 2)
        set.add(x)
    }
    set
  }

  def isInstalled(itemStack: ItemStack, module: TerminalType): Boolean ={
    if(itemStack == null || itemStack.getItem == null)
      return false
    val tag = ensureTagCompound(itemStack)
    val installed: Byte = {
      if(tag.hasKey("modules"))
        tag.getByte("modules")
      else
        0
    }
    if(1 == (installed >> module.ordinal) % 2)
      true
    else
      false

  }


  @SuppressWarnings(Array("unchecked", "rawtypes"))
  override def getSubItems(item: Item, creativeTab: CreativeTabs, itemList: util.List[_]) {
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    val tag = new NBTTagCompound
    tag.setByte("modules", 31)
    val itemStack: ItemStack = new ItemStack(item)
    itemStack.setTagCompound(tag)
    itemStack.setTagCompound(tag)
    itemList2.add(itemStack.copy)
    injectAEPower(itemStack, this.MAX_POWER)
    itemList2.add(itemStack)
  }

}
