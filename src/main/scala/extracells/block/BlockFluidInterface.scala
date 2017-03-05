package extracells.block

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.networking.IGridNode
import appeng.api.util.AEPartLocation
import buildcraft.api.tools.IToolWrench
import cpw.mods.fml.relauncher.Side
import cpw.mods.fml.relauncher.SideOnly
import extracells.api.IECTileEntity
import extracells.network.GuiHandler
import extracells.tileentity.IListenerTile
import extracells.tileentity.TileEntityFluidFiller
import extracells.tileentity.TileEntityFluidInterface
import extracells.util.PermissionUtil
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.IIcon
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection
import javax.annotation.Nullable
import java.util.Random

class BlockFluidInterface() extends BlockEC(Material.IRON, 2.0F, 10.0F) {
  override def breakBlock(world: World, pos: BlockPos, stat: IBlockState) {
    dropPatter(world, pos)
    super.breakBlock(world, pos, stat)
  }

  def createNewTileEntity(world: World, meta: Int): TileEntity = {
    return new TileEntityFluidInterface
  }

  private def dropPatter(world: World, pos: BlockPos) {
    val rand: Random = new Random
    val x: Int = pos.getX
    val y: Int = pos.getY
    val z: Int = pos.getZ
    val tileEntity: TileEntity = world.getTileEntity(pos)
    if (!(tileEntity.isInstanceOf[TileEntityFluidInterface])) {
      return
    }
    val inventory: IInventory = (tileEntity.asInstanceOf[TileEntityFluidInterface]).inventory
    var i: Int = 0
    while (i < inventory.getSizeInventory) {
      {
        val item: ItemStack = inventory.getStackInSlot(i)
        if (item != null && item.stackSize > 0) {
          val rx: Float = rand.nextFloat * 0.8F + 0.1F
          val ry: Float = rand.nextFloat * 0.8F + 0.1F
          val rz: Float = rand.nextFloat * 0.8F + 0.1F
          val entityItem: EntityItem = new EntityItem(world, x + rx, y + ry, z + rz, item.copy)
          if (item.hasTagCompound) {
            entityItem.getEntityItem.setTagCompound(item.getTagCompound.copy.asInstanceOf[NBTTagCompound])
          }
          val factor: Float = 0.05F
          entityItem.motionX = rand.nextGaussian * factor
          entityItem.motionY = rand.nextGaussian * factor + 0.2F
          entityItem.motionZ = rand.nextGaussian * factor
          world.spawnEntityInWorld(entityItem)
          item.stackSize = 0
        }
      }
      {
        i += 1; i - 1
      }
    }
  }
}