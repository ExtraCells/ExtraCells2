package extracells.integration.opencomputers

import appeng.api.networking.IGridHost
import appeng.api.networking.security.IActionHost
import appeng.api.util.AEPartLocation
import li.cil.oc.api.Network
import li.cil.oc.api.network._
import li.cil.oc.api.prefab.AbstractManagedEnvironment
import net.minecraft.tileentity.TileEntity


class UpgradeAE(envHost: EnvironmentHost) extends AbstractManagedEnvironment with NetworkControl[TileEntity with IActionHost with IGridHost]{

  setNode(Network.newNode(this, Visibility.Network).withConnector().withComponent("upgrade_me", Visibility.Neighbors).create())

  private type TileSecurityStation = TileEntity with IActionHost with IGridHost

  override def host: EnvironmentHost = envHost

  override def tile: TileSecurityStation = {
    val sec = getSecurity
    if (sec == null)
      throw new SecurityException("No Security Station")
    val node = sec.getGridNode(AEPartLocation.INTERNAL)
    if (node == null) throw new SecurityException("No Security Station")
    val gridBlock = node.getGridBlock
    if (gridBlock == null) throw new SecurityException("No Security Station")
    val coord = gridBlock.getLocation
    if (coord == null) throw new SecurityException("No Security Station")
    val tileSecurity = coord.getWorld.getTileEntity(coord.getPos).asInstanceOf[TileSecurityStation]
    if (tileSecurity == null) throw new SecurityException("No Security Station")
    tileSecurity
  }

}
