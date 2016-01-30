package extracells.integration.opencomputers

import appeng.api.features.IWirelessTermHandler
import appeng.api.util.IConfigManager
import extracells.item.ItemOCUpgrade
import li.cil.oc.common.item.data.{DroneData, RobotData}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound


object WirelessHandlerUpgradeAE extends IWirelessTermHandler{

  override def canHandle(itemStack: ItemStack): Boolean = {
    if (itemStack == null) return false
    val item = itemStack.getItem
    if (item == ItemOCUpgrade) return true
    (OCUtils.isRobot(itemStack) && OCUtils.getComponent(new RobotData(itemStack), ItemOCUpgrade) != null)||
      (OCUtils.isDrone(itemStack) && OCUtils.getComponent(new DroneData(itemStack), ItemOCUpgrade) != null)
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
    itemStack.getTagCompound.getString("key")
  }

  def setEncryptionKeyRobot(itemStack: ItemStack, encKey: String, name: String){
    val robot = new RobotData(itemStack)
    val component = OCUtils.getComponent(robot, ItemOCUpgrade)
    if (component != null) setEncryptionKey(component, encKey, name);
    robot.save(itemStack)
  }

  def getEncryptionKeyRobot(stack: ItemStack): String = {
    val robot = new RobotData(stack)
    val component = OCUtils.getComponent(robot, ItemOCUpgrade)
    if (component == null) return ""
    getEncryptionKey(component)
  }

  def setEncryptionKeyDrone(itemStack: ItemStack, encKey: String, name: String){
    val robot = new RobotData(itemStack)
    val component = OCUtils.getComponent(robot, ItemOCUpgrade)
    if (component != null) setEncryptionKey(component, encKey, name);
    robot.save(itemStack)
  }

  def getEncryptionKeyDrone(stack: ItemStack): String = {
    val drone = new DroneData(stack)
    val component = OCUtils.getComponent(drone, ItemOCUpgrade)
    if (component == null) return ""
    getEncryptionKey(component)
  }

}
