package extracells.integration.opencomputers

import extracells.item.ItemOCUpgrade
import extracells.registries.ItemEnum
import li.cil.oc.api.Manual
import li.cil.oc.api.manual.PathProvider
import li.cil.oc.api.prefab.{ItemStackTabIconRenderer, ResourceContentProvider}
import net.minecraft.item.ItemStack
import net.minecraft.world.World


object ExtraCellsPathProvider extends PathProvider{
  Manual.addProvider(this)
  Manual.addProvider(new ResourceContentProvider("extracells", "doc/"))
  Manual.addTab(new ItemStackTabIconRenderer(new ItemStack(ItemEnum.FLUIDSTORAGE.getItem)),"itemGroup.Extra_Cells", "extracells/%LANGUAGE%/index.md")

  override def pathFor(stack: ItemStack): String = if(stack != null && stack.getItem == ItemOCUpgrade) "extracells/%LANGUAGE%/me_upgrade.md" else null

  override def pathFor(world: World, x: Int, y: Int, z: Int): String = null
}
