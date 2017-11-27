package extracells.integration.opencomputers

import li.cil.oc.api.network.EnvironmentHost

object CompleteHelper {

  def getCompleteUpgradeAE(envHost: EnvironmentHost): UpgradeAE = new UpgradeAEComplete(envHost)

  def getCompleteUpgradeAEClass: Class[_ <: UpgradeAE] = classOf[UpgradeAEComplete]

}
