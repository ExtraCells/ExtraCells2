package extracells.block

import java.util.Random
import javax.annotation.Nullable

import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.util.AEPartLocation
import extracells.api.IECTileEntity
import extracells.network.GuiHandler
import extracells.tileentity.{IListenerTile, TileEntityFluidFiller, TileEntityFluidInterface}
import extracells.util.{PermissionUtil, TileUtil}
import net.minecraft.block.material.Material
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.World

class BlockFluidFiller() extends BlockEC(Material.IRON, 2.0F, 10.0F) {
  def createNewTileEntity(world: World, meta: Int): TileEntity = {
    return new TileEntityFluidFiller
  }

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, @Nullable current: ItemStack, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    val x: Int = pos.getX
    val y: Int = pos.getY
    val z: Int = pos.getZ
    if (world.isRemote) return true
    val rand: Random = new Random
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile.isInstanceOf[IECTileEntity]) if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (tile.asInstanceOf[IECTileEntity]).getGridNode(AEPartLocation.INTERNAL))) return false
    if (player.isSneaking && current != null) {
      /*try {
               if (current.getItem() instanceof IToolWrench
                   && ((IToolWrench) current.getItem()).canWrench(
                       player, x, y, z)) {
                 ItemStack block = new ItemStack(this, 1,0);
                 if (tile != null
                     && tile instanceof TileEntityFluidInterface) {
                   block.setTagCompound(((TileEntityFluidInterface) tile)
                       .writeFilter(new NBTTagCompound()));
                 }
                 dropBlockAsItem(world, pos, state, 1);
                 world.setBlockToAir(pos);
                 ((IToolWrench) current.getItem()).wrenchUsed(player, x,
                     y, z);
                 return true;
               }
             } catch (Throwable e) {
               // No IToolWrench
             }*/ if (current.getItem.isInstanceOf[IAEWrench] && (current.getItem.asInstanceOf[IAEWrench]).canWrench(current, player, pos)) {
        val block: ItemStack = new ItemStack(this, 1, 0)
        if (tile != null && tile.isInstanceOf[TileEntityFluidInterface]) {
          block.setTagCompound((tile.asInstanceOf[TileEntityFluidInterface]).writeFilter(new NBTTagCompound))
        }
        dropBlockAsItem(world, pos, state, 1)
        world.setBlockToAir(pos)
        return true
      }
    }
    GuiHandler.launchGui(0, player, world, x, y, z)
    return true
  }

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, entity: EntityLivingBase, stack: ItemStack) {
    super.onBlockPlacedBy(world, pos, state, entity, stack)
    if (world.isRemote) return
    TileUtil.setOwner(world, pos, entity)
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile != null) {
      if (tile.isInstanceOf[IListenerTile]) (tile.asInstanceOf[IListenerTile]).registerListener()
    }
  }

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState) {
    if (world.isRemote) {
      super.breakBlock(world, pos, state)
      return
    }
    TileUtil.destroy(world, pos)
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile != null) {
      if (tile.isInstanceOf[IListenerTile]) (tile.asInstanceOf[IListenerTile]).removeListener()
    }
    super.breakBlock(world, pos, state)
  }
}