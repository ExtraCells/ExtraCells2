package extracells.integration.opencomputers

import li.cil.oc.api.API
import li.cil.oc.common.item.data.RobotData
import net.minecraft.item.{Item, ItemStack}


object OCUtils {

  def isRobot(stack: ItemStack): Boolean = {
    val item = API.items.get(stack)
    if (item == null) return false
    item.name == "robot"
  }

  def getComponent(robot: RobotData, item: Item, meta: Int): ItemStack = {
    for(component <- robot.components){
      if(component != null && component.getItem == item) return component
    }
    null
  }

  def getComponent(robot: RobotData, item: Item): ItemStack = getComponent(robot, item, 0)


}
