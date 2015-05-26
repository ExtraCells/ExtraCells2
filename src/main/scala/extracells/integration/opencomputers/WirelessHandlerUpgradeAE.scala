package extracells.integration.opencomputers

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import li.cil.oc.common.item.data.{DroneData, RobotData}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


object WirelessHandlerUpgradeAE extends IWirelessTermHandler{

  override def canHandle(itemStack: ItemStack): Boolean = {
    if (itemStack == null) return false
    val item = itemStack.getItem
    if (item == ItemUpgradeAE) return true
    (OCUtils.isRobot(itemStack) && OCUtils.getComponent(new RobotData(itemStack), ItemUpgradeAE) != null)||
      (OCUtils.isDrone(itemStack) && OCUtils.getComponent(new DroneData(itemStack), ItemUpgradeAE) != null)
  }

  override def usePower(entityPlayer: EntityPlayer, v: Double, itemStack: ItemStack): Boolean = false

  override def getConfigManager(itemStack: ItemStack): IConfigManager = null

  override def hasPower(entityPlayer: EntityPlayer, v: Double, itemStack: ItemStack): Boolean = true

  override def setEncryptionKey(itemStack: ItemStack, encKey: String, name: String) {
    if(OCUtils.isRobot(itemStack)){
      setEncryptionKeyRobot(itemStack, encKey, name)
      return
    }
    if(OCUtils.isDrone(itemStack)){
      setEncryptionKeyDrone(itemStack, encKey, name)
      return
    }
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    val tagCompound: NBTTagCompound = itemStack.getTagCompound
    tagCompound.setString("key", encKey)
  }

  override def getEncryptionKey(itemStack: ItemStack): String = {
    if(OCUtils.isRobot(itemStack))
      return getEncryptionKeyRobot(itemStack)
    if(OCUtils.isDrone(itemStack))
      return getEncryptionKeyDrone(itemStack)
    if (!itemStack.hasTagCompound) itemStack.setTagCompound(new NBTTagCompound)
    return itemStack.getTagCompound.getString("key")
  }

  def setEncryptionKeyRobot(itemStack: ItemStack, encKey: String, name: String){
    val robot = new RobotData(itemStack)
    val component = OCUtils.getComponent(robot, ItemUpgradeAE)
    if (component != null) setEncryptionKey(component, encKey, name);
    robot.save(itemStack)
  }

  def getEncryptionKeyRobot(stack: ItemStack): String = {
    val robot = new RobotData(stack)
    val component = OCUtils.getComponent(robot, ItemUpgradeAE)
    if (component == null) return ""
    getEncryptionKey(component)
  }

  def setEncryptionKeyDrone(itemStack: ItemStack, encKey: String, name: String){
    val robot = new RobotData(itemStack)
    val component = OCUtils.getComponent(robot, ItemUpgradeAE)
    if (component != null) setEncryptionKey(component, encKey, name);
    robot.save(itemStack)
  }

  def getEncryptionKeyDrone(stack: ItemStack): String = {
    val drone = new DroneData(stack)
    val component = OCUtils.getComponent(drone, ItemUpgradeAE)
    if (component == null) return ""
    getEncryptionKey(component)
  }

}
