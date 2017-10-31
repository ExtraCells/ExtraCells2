package extracells.util

import java.util.Optional

import appeng.api.networking.security.{IActionHost, IActionSource}
import com.google.common.base.Preconditions
import net.minecraft.entity.player.EntityPlayer

class PlayerSource(val playerObj: EntityPlayer, val via: IActionHost) extends IActionSource {
  Preconditions.checkNotNull(playerObj)

  def player: Optional[EntityPlayer] = Optional.of[EntityPlayer](this.playerObj)

  def machine: Optional[IActionHost] = Optional.ofNullable(this.via)

  def context[T](key: Class[T]): Optional[T] = Optional.empty[T]
}
