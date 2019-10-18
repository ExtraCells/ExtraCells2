package extracells.item

import extracells.integration.opencomputers.UpgradeItemAEBase
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraftforge.fml.relauncher.Side
import net.minecraftforge.fml.relauncher.SideOnly
import extracells.models.ModelManager

object ItemOCUpgrade extends ItemECBase with UpgradeItemAEBase{

  def THIS = this

  setHasSubtypes(true)

  override def getTranslationKey(stack: ItemStack): String = getTranslationKey

  override def getTranslationKey: String = super.getTranslationKey.replace("item.extracells", "extracells.item")

  override def getItemStackDisplayName(stack: ItemStack): String = {
    val tier = 3 - stack.getItemDamage
    super.getItemStackDisplayName(stack) + " (Tier " + tier + ")"
  }

  @SideOnly(Side.CLIENT)
  override def getSubItems(tab: CreativeTabs, subItems: NonNullList[ItemStack]) {
    if (!this.isInCreativeTab(tab)) return
    subItems.add(new ItemStack(this, 1, 2))
    subItems.add(new ItemStack(this, 1, 1))
    subItems.add(new ItemStack(this, 1, 0))
  }

  @SideOnly(Side.CLIENT) override def registerModel(item: Item, manager: ModelManager) {
    var i = 0
    while (i < 3) {
      {
        manager.registerItemModel(item, i)
      }
      {
        i += 1; i - 1
      }
    }
  }
}
