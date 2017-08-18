package extracells.item

/*@Optional.Interface(iface = "thaumicenergistics.api.IThEWirelessEssentiaTerminal", modid = "thaumicenergistics", striprefs = true)
trait EssensiaTerminal extends Item with IThEWirelessEssentiaTerminal{


  override def getWETerminalTag(terminalItemstack: ItemStack): NBTTagCompound = {
    val tag = ensureTagCompound(terminalItemstack)
    if (!tag.hasKey("essentia"))
      tag.setTag("essentia", new NBTTagCompound)
    return tag.getCompoundTag("essentia")
  }

  private def ensureTagCompound(itemStack: ItemStack): NBTTagCompound = {
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    return itemStack.getTagCompound
  }

}*/
