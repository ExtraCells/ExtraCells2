package extracells.integration.opencomputers

import li.cil.oc.api.API
import li.cil.oc.common.item.data.{DroneData, RobotData}
import net.minecraft.item.{Item, ItemStack}


object OCUtils {

  def isRobot(stack: ItemStack): Boolean = {
    val item = API.items.get(stack)
    if (item == null) return false
    item.name == "robot"
  }

  def isDrone(stack: ItemStack): Boolean = {
    val item = API.items.get(stack)
    if (item == null) return false
    item.name == "drone"
  }

  def getComponent(robot: RobotData, item: Item, meta: Int): ItemStack = {
    for(component <- robot.components){
      if(component != null && component.getItem == item) return component
    }
    null
  }

  def getComponent(robot: RobotData, item: Item): ItemStack = getComponent(robot, item, 0)

  def getComponent(drone: DroneData, item: Item, meta: Int): ItemStack = {
    for(component <- drone.components){
      if(component != null && component.getItem == item) return component
    }
    null
  }

  def getComponent(drone: DroneData, item: Item): ItemStack = getComponent(drone, item, 0)


}
