package extracells.item

import java.util
import cpw.mods.fml.relauncher.{Side, SideOnly}
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.EnumRarity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.IIcon
import net.minecraft.util.StatCollector
import net.minecraft.world.World
import net.minecraftforge.fluids.Fluid
import net.minecraftforge.fluids.FluidRegistry
import appeng.api.AEApi
import appeng.api.config.AccessRestriction
import appeng.api.config.FuzzyMode
import appeng.api.storage.IMEInventoryHandler
import appeng.api.storage.StorageChannel
import appeng.api.storage.data.IAEFluidStack
import extracells.api.ECApi
import extracells.api.IHandlerFluidStorage
import extracells.api.IPortableFluidStorageCell
import extracells.util.inventory.ECFluidFilterInventory
import extracells.util.inventory.ECPrivateInventory

object ItemStoragePortableCell extends ItemECBase with IPortableFluidStorageCell with PowerItem {

  override val MAX_POWER: Double = 20000
  private[item] var icon: IIcon = null
  def THIS = this
  setMaxStackSize(1)
  setMaxDamage(0)


  @SuppressWarnings(Array("rawtypes", "unchecked")) override def addInformation(itemStack: ItemStack, player: EntityPlayer, list: util.List[_], par4: Boolean) {
    val list2 = list.asInstanceOf[util.List[String]]
    val handler: IMEInventoryHandler[IAEFluidStack] = AEApi.instance.registries.cell.getCellInventory(itemStack, null, StorageChannel.FLUIDS).asInstanceOf[IMEInventoryHandler[IAEFluidStack]]

    if (!(handler.isInstanceOf[IHandlerFluidStorage])) {
      return
    }
    val cellHandler: IHandlerFluidStorage = handler.asInstanceOf[IHandlerFluidStorage]
    val partitioned: Boolean = cellHandler.isFormatted
    val usedBytes: Long = cellHandler.usedBytes
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.bytes"), (usedBytes / 250).asInstanceOf[AnyRef], (cellHandler.totalBytes / 250).asInstanceOf[AnyRef]))
    list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes.asInstanceOf[AnyRef], cellHandler.totalTypes.asInstanceOf[AnyRef]))
    if (usedBytes != 0) {
      list2.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.fluid.content"), usedBytes.asInstanceOf[AnyRef]))
    }
    if (partitioned) {
      list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal("gui.appliedenergistics2.Precise"))
    }
    list2.add(StatCollector.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / ItemStoragePortableCell.MAX_POWER * 1e4) / 1e2 + "%")
  }

  def getConfigInventory(is: ItemStack): IInventory = {
    return new ECFluidFilterInventory("configFluidCell", 63, is)
  }

  override def getDurabilityForDisplay(itemStack: ItemStack): Double = {
    return 1 - getAECurrentPower(itemStack) / ItemStoragePortableCell.MAX_POWER
  }


  def getFilter(stack: ItemStack): util.ArrayList[Fluid] = {
    val inventory: ECFluidFilterInventory = new ECFluidFilterInventory("", 63, stack)
    val stacks: Array[ItemStack] = inventory.slots
    val filter: util.ArrayList[Fluid] = new util.ArrayList[Fluid]
    if (stacks.length == 0) return null
    for (s <- stacks) {
      if (s != null) {
        val f: Fluid = FluidRegistry.getFluid(s.getItemDamage)
        if (f != null) filter.add(f)
      }
    }
    return filter
  }

  def getFuzzyMode(is: ItemStack): FuzzyMode = {
    if (is == null) return null
    if (!is.hasTagCompound) is.setTagCompound(new NBTTagCompound)
    if (is.getTagCompound.hasKey("fuzzyMode")) return FuzzyMode.valueOf(is.getTagCompound.getString("fuzzyMode"))
    is.getTagCompound.setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name)
    return FuzzyMode.IGNORE_ALL
  }

  override def getIconFromDamage(dmg: Int): IIcon = {
    return this.icon
  }

  def getMaxBytes(is: ItemStack): Int = {
    return 512
  }


  def getMaxTypes(unused: ItemStack): Int = {
    return 3
  }

  override def getPowerFlow(itemStack: ItemStack): AccessRestriction = {
    return AccessRestriction.READ_WRITE
  }

  override def getRarity(itemStack: ItemStack): EnumRarity = {
    return EnumRarity.rare
  }

  override def getSubItems(item: Item, creativeTab: CreativeTabs, itemList: util.List[_]) {
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(item))
    val itemStack: ItemStack = new ItemStack(item)
    injectAEPower(itemStack, ItemStoragePortableCell.MAX_POWER)
    itemList2.add(itemStack)
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return "extracells.item.storage.fluid.portable"
  }

  def getUpgradesInventory(is: ItemStack): IInventory = {
    return new ECPrivateInventory("configInventory", 0, 64)
  }

  def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    return getAECurrentPower(is) >= amount
  }

  def isEditable(is: ItemStack): Boolean = {
    if (is == null) return false
    return is.getItem eq this
  }

  @SuppressWarnings(Array("rawtypes", "unchecked"))
  override def onItemRightClick(itemStack: ItemStack, world: World, player: EntityPlayer): ItemStack = {
    return ECApi.instance.openPortableCellGui(player, itemStack, world)
  }


  @SideOnly(Side.CLIENT)
  override def registerIcons(iconRegister: IIconRegister) {
    this.icon = iconRegister.registerIcon("extracells:storage.fluid.portable")
  }

  def setFuzzyMode(is: ItemStack, fzMode: FuzzyMode) {
    if (is == null) return
    if (!is.hasTagCompound) is.setTagCompound(new NBTTagCompound)
    val tag: NBTTagCompound = is.getTagCompound
    tag.setString("fuzzyMode", fzMode.name)
  }

  override def showDurabilityBar(itemStack: ItemStack): Boolean = {
    return true
  }

  def usePower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    extractAEPower(is, amount)
    return true
  }
}
