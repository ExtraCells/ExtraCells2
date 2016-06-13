package extracells.util.recipe

import appeng.api.features.INetworkEncodable
import appeng.api.implementations.items.IAEItemPowerStorage
import extracells.item.{ItemWirelessTerminalUniversal, TerminalType}
import extracells.registries.ItemEnum
import extracells.util.UniversalTerminal
import net.minecraft.inventory.InventoryCrafting
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipe
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.world.World


object RecipeUniversalTerminal extends IRecipe{

  val THIS = this

  val itemUniversal = ItemWirelessTerminalUniversal

  override def matches(inventory : InventoryCrafting, world : World): Boolean = {
    var hasWireless = false
    var isUniversal = false
    var hasTerminal = false
    var terminals = List[TerminalType]()
    var terminal: ItemStack = null
    val size = inventory.getSizeInventory
    var i = 0
    for(i <- 0 until size){
      val stack = inventory.getStackInSlot(i)
      if(stack != null){
        val item = stack.getItem
        if(item == itemUniversal){
          if(hasWireless)
            return false
          else{
            hasWireless = true
            isUniversal = true
            terminal = stack
          }
        }else if(UniversalTerminal.isWirelessTerminal(stack)){
          if(hasWireless)
            return false
          hasWireless = true
          terminal = stack
        }else if(UniversalTerminal.isTerminal(stack)){
          hasTerminal = true
          val typeTerminal = UniversalTerminal.getTerminalType(stack)
          if(terminals.contains(typeTerminal)){
            return false
          }else{
            terminals ++= List(typeTerminal)
          }
        }
      }
    }
    if(!(hasTerminal && hasWireless))
     return false
    if(isUniversal){
      for(x <- terminals){
        if(itemUniversal.isInstalled(terminal, x))
          return false
      }
      true
    }else{
      val terminalType = UniversalTerminal.getTerminalType(terminal)
      for(x <- terminals){
        if(x == terminalType)
          return false
      }
      true
    }
  }

  override def getRecipeOutput: ItemStack = ItemEnum.UNIVERSALTERMINAL.getDamagedStack(0)

  override def getRecipeSize: Int = 2

  override def getCraftingResult(inventory : InventoryCrafting): ItemStack = {
    var isUniversal = false
    var terminals = List[TerminalType]()
    var terminal: ItemStack = null
    val size = inventory.getSizeInventory
    var i = 0
    for(i <- 0 until size){
      val stack = inventory.getStackInSlot(i)
      if(stack != null){
        val item = stack.getItem
        if(item == itemUniversal){
            isUniversal = true
            terminal = stack.copy
        }else if(UniversalTerminal.isWirelessTerminal(stack)){
          terminal = stack.copy
        }else if(UniversalTerminal.isTerminal(stack)){
          val typeTerminal = UniversalTerminal.getTerminalType(stack)
        terminals ++= List(typeTerminal)

        }
      }
    }
    if(isUniversal){
      for(x <- terminals)
        itemUniversal.installModule(terminal, x)
    }else{
      val terminalType = UniversalTerminal.getTerminalType(terminal)
      val itemTerminal = terminal.getItem
      val t = new ItemStack(itemUniversal)
      if(itemTerminal.isInstanceOf[INetworkEncodable]){
        val key = itemTerminal.asInstanceOf[INetworkEncodable].getEncryptionKey(terminal)
        if(key != null)
          itemUniversal.setEncryptionKey(t, key, null)
      }
      if(itemTerminal.isInstanceOf[IAEItemPowerStorage]){
        val power = itemTerminal.asInstanceOf[IAEItemPowerStorage].getAECurrentPower(terminal)
        itemUniversal.injectAEPower(t, power)
      }
      if(terminal.hasTagCompound){
        val nbt = terminal.getTagCompound
        if(!t.hasTagCompound)
          t.setTagCompound(new NBTTagCompound)
        if(nbt.hasKey("BoosterSlot")){
          t.getTagCompound.setTag("BoosterSlot", nbt.getTag("BoosterSlot"))
        }
        if(nbt.hasKey("MagnetSlot"))
          t.getTagCompound.setTag("MagnetSlot", nbt.getTag("MagnetSlot"));
      }
      itemUniversal.installModule(t, terminalType)
      t.getTagCompound.setByte("type", terminalType.ordinal.toByte)
      terminal = t
      for(x <- terminals)
        itemUniversal.installModule(terminal, x)
    }
    terminal
  }
}
