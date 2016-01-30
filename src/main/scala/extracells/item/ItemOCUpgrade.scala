package extracells.item

import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.integration.opencomputers.UpgradeItemAEBase
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.{Item, ItemStack}

object ItemOCUpgrade extends ItemECBase with UpgradeItemAEBase{

  setTextureName("extracells:upgrade.oc")

  def THIS = this

  override def getUnlocalizedName = super.getUnlocalizedName.replace("item.extracells", "extracells.item")

  override def getUnlocalizedName(stack: ItemStack) = getUnlocalizedName

  @SideOnly(Side.CLIENT)
  override def getSubItems(item : Item, tab : CreativeTabs, list : java.util.List[_]) {
    def add[T](list: java.util.List[T], value: Any) = list.add(value.asInstanceOf[T])
    add(list, new ItemStack(item, 1, 2))
    add(list, new ItemStack(item, 1, 1))
    add(list, new ItemStack(item, 1, 0))
  }

  override def getItemStackDisplayName(stack : ItemStack): String = {
    val tier = stack.getItemDamage match {
      case 0 => 3
      case 1 => 2
      case _ => 1
    }
    super.getItemStackDisplayName(stack) + " (Tier " + tier + ")"
  }

}
