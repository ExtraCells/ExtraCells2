package extracells.integration.opencomputers

import appeng.api.parts.{IPart, IPartHost}
import appeng.api.util.AEPartLocation
//import li.cil.oc.common.item.data.{DroneData, RobotData}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World


object OCUtils {

  def getPart[P>: IPart ](world: World, pos: BlockPos, location: AEPartLocation): P = {
    val tile = world.getTileEntity(pos)
    if (tile == null || (!tile.isInstanceOf[IPartHost])) return null
    val host = tile.asInstanceOf[IPartHost]
    if (location == null || (location eq AEPartLocation.INTERNAL)) {
      for (side <- AEPartLocation.SIDE_LOCATIONS) {
        val part = host.getPart(side)
        if (part != null && part.isInstanceOf[P]) return part.asInstanceOf[P]
      }
      return null
    } else {
      val part = host.getPart(location)
      return if (part == null || !part.isInstanceOf[P]) null
      else part.asInstanceOf[P]
    }
  }

 /* def isRobot(stack: ItemStack): Boolean = {
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

  def getComponent(drone: DroneData, item: Item): ItemStack = getComponent(drone, item, 0)*/


}
