package extracells.block

import java.util.Random

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.networking.IGridNode
import appeng.api.util.AEPartLocation
import extracells.container.ContainerHardMEDrive
import extracells.network.GuiHandler
import extracells.tileentity.TileEntityHardMeDrive
import extracells.util.PermissionUtil
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, MathHelper}
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.World


object BlockHardMEDrive extends BlockEC(net.minecraft.block.material.Material.ROCK, 2.0F, 1000000.0F) with TGuiBlock{

  override def getServerGuiElement(player: EntityPlayer, world: World,  pos: BlockPos): Any = {
    val tile = world.getTileEntity(pos)
    if (tile == null || player == null) return null
    tile match {
      case tileMe: TileEntityHardMeDrive => {
        return new ContainerHardMEDrive(player.inventory, tileMe)
      }
      case _ => return null
    }
  }

  //Only needed because BlockEnum is in java. not in scala
  val instance = this


  setUnlocalizedName("block.hardmedrive");

  override def createNewTileEntity(world : World, meta : Int): TileEntity = new TileEntityHardMeDrive()

  private def dropItems(world: World, pos: BlockPos) {
    val x = pos.getX
    val y = pos.getY
    val z = pos.getZ
    val rand: Random = new Random
    val tileEntity: TileEntity = world.getTileEntity(pos)
    if (!(tileEntity.isInstanceOf[TileEntityHardMeDrive])) {
      return
    }
    val inventory: IInventory = (tileEntity.asInstanceOf[TileEntityHardMeDrive]).getInventory

    var i: Int = 0
    while (i < inventory.getSizeInventory) {

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
      i += 1

    }
  }

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, heldItem: ItemStack, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (world.isRemote) return false
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile.isInstanceOf[TileEntityHardMeDrive]) if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(AEPartLocation.INTERNAL))) return false
    val current: ItemStack = player.inventory.getCurrentItem
    if (player.isSneaking && current != null) {
      //TODO: Add buildcraft Support
      /*try {
        if (current.getItem.isInstanceOf[IToolWrench] && (current.getItem.asInstanceOf[IToolWrench]).canWrench(player, x, y, z)) {
          dropBlockAsItem(world, x, y, z, new ItemStack(this))
          world.setBlockToAir(x, y, z)
          (current.getItem.asInstanceOf[IToolWrench]).wrenchUsed(player, x, y, z)
          return true
        }
      }
      catch {
        case e: Throwable => {
        }
      }*/
      if (current.getItem.isInstanceOf[IAEWrench] && (current.getItem.asInstanceOf[IAEWrench]).canWrench(current, player, pos)) {
        dropBlockAsItem(world, pos, world.getBlockState(pos), 1)
        world.setBlockToAir(pos)
        return true
      }
    }
    GuiHandler.launchGui(0, player, world, pos.getX, pos.getY, pos.getZ)
    return true
  }

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, entity: EntityLivingBase, stack: ItemStack) {
    super.onBlockPlacedBy(world, pos, state, entity, stack)
    val l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

    //TODO: Add rotation
    /*if (!entity.isSneaking())
    {
      if (l == 0)
      {
        world.setBlockMetadataWithNotify(x, y, z, 2, 2);
      }

      if (l == 1)
      {
        world.setBlockMetadataWithNotify(x, y, z, 5, 2);
      }

      if (l == 2)
      {
        world.setBlockMetadataWithNotify(x, y, z, 3, 2);
      }

      if (l == 3)
      {
        world.setBlockMetadataWithNotify(x, y, z, 4, 2);
      }
    } else
    {
      if (l == 0)
      {
        world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(2).getOpposite().ordinal(), 2);
      }

      if (l == 1)
      {
        world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(5).getOpposite().ordinal(), 2);
      }

      if (l == 2)
      {
        world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(3).getOpposite().ordinal(), 2);
      }

      if (l == 3)
      {
        world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(4).getOpposite().ordinal(), 2);
      }
    }*/
    if (world.isRemote) return
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile != null) {
      if (tile.isInstanceOf[TileEntityHardMeDrive]) {
        val node: IGridNode = (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(AEPartLocation.INTERNAL)
        if (entity != null && entity.isInstanceOf[EntityPlayer]) {
          val player: EntityPlayer = entity.asInstanceOf[EntityPlayer]
          node.setPlayerID(AEApi.instance.registries.players.getID(player))
        }
        node.updateState
      }
    }
  }

  override def breakBlock(world: World, pos: BlockPos, state: IBlockState) {
    if (world.isRemote) return
    dropItems(world, pos)
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile != null) {
      if (tile.isInstanceOf[TileEntityHardMeDrive]) {
        val node: IGridNode = (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(AEPartLocation.INTERNAL)
        if (node != null) {
          node.destroy
        }
      }
    }

    super.breakBlock(world, pos, state)
  }

}
