package extracells.util

import appeng.api.AEApi
import extracells.integration.Integration.Mods
import extracells.integration.WirelessCrafting.WirelessCrafting
import extracells.integration.thaumaticenergistics.ThaumaticEnergistics
import extracells.item.TerminalType
import extracells.registries.{ItemEnum, PartEnum}
import net.minecraft.item.ItemStack


object UniversalTerminal {
  val isMekLoaded = Mods.MEKANISMGAS.isEnabled
  val isThaLoaded = Mods.THAUMATICENERGISTICS.isEnabled
  val isWcLLoaded = Mods.WIRELESSCRAFTING.isEnabled
  val arrayLength = {
    var length = 2
    if(isMekLoaded)
      length += 1
    if(isThaLoaded)
      length += 1
    if(isWcLLoaded)
      length += 1
    length
  }

  val wirelessTerminals : Array[ItemStack] = {
    val terminals = new Array[ItemStack](arrayLength)
    terminals.update(0, AEApi.instance.definitions.items.wirelessTerminal.maybeStack(1).get())
    terminals.update(1, ItemEnum.FLUIDWIRELESSTERMINAL.getSizedStack(1))
    var next = 2
    if(isMekLoaded) {
      terminals.update(next, ItemEnum.GASWIRELESSTERMINAL.getSizedStack(1))
      next += 1
    }
    if(isThaLoaded) {
      terminals.update(next, ThaumaticEnergistics.getWirelessTerminal)
      next += 1
    }
    if(isWcLLoaded)
      terminals.update(next, WirelessCrafting.getCraftingTerminal)
    terminals
  }

  val terminals : Array[ItemStack] = {
    val terminals = new Array[ItemStack](if (isWcLLoaded) arrayLength - 1 else arrayLength)
    terminals.update(0, AEApi.instance.definitions.parts.terminal.maybeStack(1).get())
    terminals.update(1, ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal))
    if(isMekLoaded) {
      terminals.update(2, ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal))
      if(isThaLoaded)
        terminals.update(3, ThaumaticEnergistics.getTerminal)
    }else if(isThaLoaded)
      terminals.update(2, ThaumaticEnergistics.getTerminal)
    terminals
  }

  def isTerminal(stack: ItemStack): Boolean = {
    if(stack == null)
      return false
    val item = stack.getItem
    val meta = stack.getItemDamage
    if(item == null)
      return false
    val aeterm = AEApi.instance.definitions.parts.terminal.maybeStack(1).get
    if(item == aeterm.getItem && meta == aeterm.getItemDamage)
      return true
    val ecterm = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal)
    if(item == ecterm.getItem && meta == ecterm.getItemDamage)
      return true
    val ectermgas = ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal)
    if(item == ectermgas.getItem && meta == ectermgas.getItemDamage)
      return true
    if(Mods.THAUMATICENERGISTICS.isEnabled){
      val thterm = ThaumaticEnergistics.getTerminal
      if(item == thterm.getItem && meta == thterm.getItemDamage)
        return true
    }
    false
  }

  def isWirelessTerminal(stack: ItemStack): Boolean = {
    if(stack == null)
      return false
    val item = stack.getItem
    val meta = stack.getItemDamage
    if(item == null)
      return false
    val aeterm = AEApi.instance.definitions.items.wirelessTerminal.maybeStack(1).get
    if(item == aeterm.getItem && meta == aeterm.getItemDamage)
      return true
    val ecterm = ItemEnum.FLUIDWIRELESSTERMINAL.getDamagedStack(0)
    if(item == ecterm.getItem && meta == ecterm.getItemDamage)
      return true
    val ectermgas = ItemEnum.GASWIRELESSTERMINAL.getDamagedStack(0)
    if(item == ectermgas.getItem && meta == ectermgas.getItemDamage)
      return true
    if(Mods.THAUMATICENERGISTICS.isEnabled){
      val thterm = ThaumaticEnergistics.getWirelessTerminal
      if(item == thterm.getItem && meta == thterm.getItemDamage)
        return true
    }
    if(isWcLLoaded){
      val wcTerm = WirelessCrafting.getCraftingTerminal
      if(item == wcTerm.getItem && meta == wcTerm.getItemDamage)
        return true
    }
    false
  }

  def getTerminalType(stack: ItemStack): TerminalType = {
    if(stack == null)
      return null
    val item = stack.getItem
    val meta = stack.getItemDamage
    if(item == null)
      return null
    val aeterm = AEApi.instance.definitions.parts.terminal.maybeStack(1).get
    if(item == aeterm.getItem && meta == aeterm.getItemDamage)
      return TerminalType.ITEM
    val ecterm = ItemEnum.PARTITEM.getDamagedStack(PartEnum.FLUIDTERMINAL.ordinal)
    if(item == ecterm.getItem && meta == ecterm.getItemDamage)
      return TerminalType.FLUID
    val ectermgas = ItemEnum.PARTITEM.getDamagedStack(PartEnum.GASTERMINAL.ordinal)
    if(item == ectermgas.getItem && meta == ectermgas.getItemDamage)
      return TerminalType.GAS
    if(Mods.THAUMATICENERGISTICS.isEnabled){
      val thterm = ThaumaticEnergistics.getTerminal
      if(item == thterm.getItem && meta == thterm.getItemDamage)
        return TerminalType.ESSENTIA
    }
    val aeterm2 = AEApi.instance.definitions.items.wirelessTerminal.maybeStack(1).get
    if(item == aeterm2.getItem && meta == aeterm2.getItemDamage)
      return TerminalType.ITEM
    val ecterm2 = ItemEnum.FLUIDWIRELESSTERMINAL.getDamagedStack(0)
    if(item == ecterm2.getItem && meta == ecterm2.getItemDamage)
      return TerminalType.FLUID
    val ectermgas2 = ItemEnum.GASWIRELESSTERMINAL.getDamagedStack(0)
    if(item == ectermgas2.getItem && meta == ectermgas2.getItemDamage)
      return TerminalType.GAS
    if(Mods.THAUMATICENERGISTICS.isEnabled){
      val thterm = ThaumaticEnergistics.getWirelessTerminal
      if(item == thterm.getItem && meta == thterm.getItemDamage)
        return TerminalType.ESSENTIA
    }
    if(isWcLLoaded){
      val wcTerm = WirelessCrafting.getCraftingTerminal
      if(item == wcTerm.getItem && meta == wcTerm.getItemDamage)
        return TerminalType.CRAFTING
    }
    null
  }

}
