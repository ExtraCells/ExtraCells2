package extracells.integration.opencomputers


import appeng.api.networking.IGridHost
import appeng.api.networking.security.IActionHost
import appeng.api.util.AEPartLocation
import li.cil.oc.api.network.EnvironmentHost
import li.cil.oc.integration.{appeng, ec}
import net.minecraft.tileentity.TileEntity

class UpgradeAEComplete(envHost: EnvironmentHost) extends UpgradeAE(envHost) with appeng.NetworkControl[TileEntity with IActionHost with IGridHost]
  with ec.NetworkControl[TileEntity with IActionHost with IGridHost]{

  override def pos: AEPartLocation = AEPartLocation.INTERNAL

}
