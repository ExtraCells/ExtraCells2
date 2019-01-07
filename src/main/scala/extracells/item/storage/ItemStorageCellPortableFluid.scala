package extracells.item.storage

import java.util

import appeng.api.AEApi
import appeng.api.config.{AccessRestriction, Actionable, FuzzyMode}
import appeng.api.storage.data.IAEFluidStack
import appeng.api.storage.IMEInventoryHandler
import extracells.api.{ECApi, IHandlerFluidStorage, IPortableFluidStorageCell}
import extracells.inventory.{ECFluidFilterInventory, InventoryPlain}
import extracells.item.{ItemECBase, ItemFluid, PowerItem}
import extracells.models.ModelManager
import extracells.util.StorageChannels
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.{EnumRarity, Item, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.translation.I18n
import net.minecraft.util.{ActionResult, EnumActionResult, EnumHand, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.fluids.{Fluid, FluidRegistry}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.items.IItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

object ItemStorageCellPortableFluid extends ItemECBase with IPortableFluidStorageCell with PowerItem {

  override val MAX_POWER: Double = 20000

  def THIS = this

  setMaxStackSize(1)
  setMaxDamage(0)

  @SuppressWarnings(Array("rawtypes", "unchecked")) override def addInformation(itemStack: ItemStack, world: World, list: util.List[String], par4: ITooltipFlag) {
    val list2 = list.asInstanceOf[util.List[String]]
    val handler: IMEInventoryHandler[IAEFluidStack] = AEApi.instance.registries.cell.getCellInventory(itemStack, null, StorageChannels.FLUID)

    if (!(handler.isInstanceOf[IHandlerFluidStorage])) {
      return
    }
    val cellHandler: IHandlerFluidStorage = handler.asInstanceOf[IHandlerFluidStorage]
    val partitioned: Boolean = cellHandler.isFormatted
    val usedBytes: Long = cellHandler.usedBytes
    val storedCount: Long = cellHandler.storedCount
    val aeCurrentPower: Double = getAECurrentPower(itemStack)
    list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.bytes"), (usedBytes).asInstanceOf[AnyRef], (cellHandler.totalBytes).asInstanceOf[AnyRef]))
    list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.types"), cellHandler.usedTypes.asInstanceOf[AnyRef], cellHandler.totalTypes.asInstanceOf[AnyRef]))
    if (storedCount != 0) {
      list2.add(String.format(I18n.translateToLocal("extracells.tooltip.storage.fluid.content"), storedCount.asInstanceOf[AnyRef]))
    }
    if (partitioned) {
      list2.add(I18n.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + I18n.translateToLocal("gui.appliedenergistics2.Precise"))
    }
    list2.add(I18n.translateToLocal("gui.appliedenergistics2.StoredEnergy") + ": " + aeCurrentPower + " AE - " + Math.floor(aeCurrentPower / ItemStorageCellPortableFluid.MAX_POWER * 1e4) / 1e2 + "%")
  }

  def getConfigInventory(is: ItemStack): IItemHandler = {
    return new InvWrapper(new ECFluidFilterInventory("configFluidCell", 63, is))
  }

  override def getDurabilityForDisplay(itemStack: ItemStack): Double = {
    return 1 - getAECurrentPower(itemStack) / ItemStorageCellPortableFluid.MAX_POWER
  }


  def getFilter(stack: ItemStack): util.ArrayList[Fluid] = {
    val inventory: ECFluidFilterInventory = new ECFluidFilterInventory("", 63, stack)
    val stacks: Array[ItemStack] = inventory.slots
    val filter: util.ArrayList[Fluid] = new util.ArrayList[Fluid]
    if (stacks.length == 0) return null
    for (s <- stacks) {
      if (s != null) {
        val fluid: Fluid = FluidRegistry.getFluid(ItemFluid.getFluidName(stack))
        if (fluid != null) filter.add(fluid)
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

  def getMaxBytes(is: ItemStack): Int = {
    return 512
  }


  def getMaxTypes(unused: ItemStack): Int = {
    return 3
  }

  override def getPowerFlow(itemStack: ItemStack): AccessRestriction = {
    return AccessRestriction.READ_WRITE
  }

  override def getRarity(itemStack: ItemStack): EnumRarity = EnumRarity.RARE


  override def getSubItems(creativeTab: CreativeTabs, itemList: NonNullList[ItemStack]) {
    if (!this.isInCreativeTab(creativeTab)) return
    val itemList2 = itemList.asInstanceOf[util.List[ItemStack]]
    itemList2.add(new ItemStack(this))
    val itemStack: ItemStack = new ItemStack(this)
    injectAEPower(itemStack, ItemStorageCellPortableFluid.MAX_POWER, Actionable.MODULATE)
    itemList2.add(itemStack)
  }

  override def getUnlocalizedName(itemStack: ItemStack): String = {
    return "extracells.item.storage.fluid.portable"
  }

  def getUpgradesInventory(is: ItemStack): IItemHandler = {
    return new InvWrapper(new InventoryPlain("configInventory", 0, 64))
  }

  def hasPower(player: EntityPlayer, amount: Double, is: ItemStack): Boolean = {
    return getAECurrentPower(is) >= amount
  }

  def isEditable(is: ItemStack): Boolean = {
    if (is == null) return false
    is.getItem == this
  }

  @SuppressWarnings(Array("rawtypes", "unchecked"))
  override def onItemRightClick(world: World, player: EntityPlayer, hand: EnumHand): ActionResult[ItemStack] = {
    new ActionResult(EnumActionResult.SUCCESS, ECApi.instance.openPortableFluidCellGui(player, hand, world))
  }

  @SideOnly(Side.CLIENT)
  override def registerModel(item: Item, manager: ModelManager) =
    manager.registerItemModel(item, 0, "storage/fluid/portable")


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
    extractAEPower(is, amount, Actionable.MODULATE)
    return true
  }
}
