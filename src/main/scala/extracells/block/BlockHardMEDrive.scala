package extracells.block

import java.util.Random

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.networking.IGridNode
import appeng.api.util.AEPartLocation
import extracells.block.properties.PropertyDrive
import extracells.container.ContainerHardMEDrive
import extracells.gui.GuiHardMEDrive
import extracells.models.drive.DriveSlotsState
import extracells.network.GuiHandler
import extracells.tileentity.TileEntityHardMeDrive
import extracells.util.{PermissionUtil, TileUtil, WrenchUtil}
import net.minecraft.block.BlockHorizontal
import net.minecraft.block.properties.{IProperty, PropertyDirection}
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.{BlockPos, MathHelper, RayTraceResult, Vec3d}
import net.minecraft.util._
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{ExtendedBlockState, IExtendedBlockState, IUnlistedProperty}


object BlockHardMEDrive extends BlockEC(net.minecraft.block.material.Material.ROCK, 2.0F, 1000000.0F)  {

  /*
  override def getServerGuiElement(player: EntityPlayer, world: World, pos: BlockPos): Any = {
    val tile = world.getTileEntity(pos)
    if (tile == null || player == null) return null
    tile match {
      case tileMe: TileEntityHardMeDrive => {
        return new ContainerHardMEDrive(player.inventory, tileMe)
      }
      case _ => return null
    }
  }

  @SideOnly(Side.CLIENT)
  override def getClientGuiElement(player: EntityPlayer, world: World, pos: BlockPos): Any = {
    val tile = world.getTileEntity(pos)
    if (tile == null || player == null) return null
    tile match {
      case tileMe: TileEntityHardMeDrive => {
        return new GuiHardMEDrive(player.inventory, tileMe)
      }
      case _ => return null
    }
  }*/
  var _FACING: PropertyDirection = null

  def FACING: PropertyDirection = {
    if (_FACING == null)
      _FACING = BlockHorizontal.FACING
    _FACING
  }

  //Only needed because BlockEnum is in java. not in scala
  val instance = this

  setUnlocalizedName("block.hardmedrive");

  override def createNewTileEntity(world: World, meta: Int): TileEntity = new TileEntityHardMeDrive()

  override def getStateForPlacement(world: World, pos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase, hand: EnumHand): IBlockState = getDefaultState.withProperty(FACING, placer.getHorizontalFacing.getOpposite)

  private def dropItems(world: World, pos: BlockPos) {
    val x = pos.getX
    val y = pos.getY
    val z = pos.getZ
    val rand = new Random
    val tileEntity = world.getTileEntity(pos)
    if (!(tileEntity.isInstanceOf[TileEntityHardMeDrive])) {
      return
    }
    val inventory = (tileEntity.asInstanceOf[TileEntityHardMeDrive]).getInventory

    var i = 0
    while (i < inventory.getSizeInventory) {

      val item = inventory.getStackInSlot(i)
      if (item != null && item.getCount > 0) {
        val rx = rand.nextFloat * 0.8F + 0.1F
        val ry = rand.nextFloat * 0.8F + 0.1F
        val rz = rand.nextFloat * 0.8F + 0.1F
        val entityItem = new EntityItem(world, x + rx, y + ry, z + rz, item.copy)
        if (item.hasTagCompound) {
          entityItem.getItem.setTagCompound(item.getTagCompound.copy.asInstanceOf[NBTTagCompound])
        }
        val factor = 0.05F
        entityItem.motionX = rand.nextGaussian * factor
        entityItem.motionY = rand.nextGaussian * factor + 0.2F
        entityItem.motionZ = rand.nextGaussian * factor
        world.spawnEntity(entityItem)
        item.setCount(0)
      }
      i += 1

    }
  }

  override def onBlockActivated(world: World, pos: BlockPos, state: IBlockState, player: EntityPlayer, hand: EnumHand, side: EnumFacing, hitX: Float, hitY: Float, hitZ: Float): Boolean = {
    if (world.isRemote) return true
    val tile: TileEntity = world.getTileEntity(pos)
    if (tile.isInstanceOf[TileEntityHardMeDrive]) if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(AEPartLocation.INTERNAL))) return false
    val current: ItemStack = player.inventory.getCurrentItem
    if (player.isSneaking) {
      val rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos)
      val wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand)
      if (wrenchHandler != null) {
        dropBlockAsItem(world, pos, world.getBlockState(pos), 1)
        world.setBlockToAir(pos)
        wrenchHandler.wrenchUsed(current, player, rayTraceResult, hand)
        return true
      }
    }
    GuiHandler.launchGui(0, player, world, pos.getX, pos.getY, pos.getZ)
    true
  }

  override def onBlockPlacedBy(world: World, pos: BlockPos, state: IBlockState, entity: EntityLivingBase, stack: ItemStack) {
    super.onBlockPlacedBy(world, pos, state, entity, stack)
    val l = MathHelper.floor(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

    world.setBlockState(pos, state.withProperty(FACING, entity.getHorizontalFacing.getOpposite), 2)

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
    if (world.isRemote) {
      super.breakBlock(world, pos, state)
      return
    }
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

  override def getBlockLayer: BlockRenderLayer = BlockRenderLayer.CUTOUT

  override protected def createBlockState =
    new ExtendedBlockState(this, Array[IProperty[_ <: Comparable[_]]](FACING), Array[IUnlistedProperty[_]](PropertyDrive.INSTANCE))

  override def getExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos): IBlockState = {
    val te = TileUtil.getTile(world, pos, classOf[TileEntityHardMeDrive])
    if (te == null) return super.getExtendedState(state, world, pos)
    val extState = super.getExtendedState(state, world, pos).asInstanceOf[IExtendedBlockState]
    extState.withProperty(PropertyDrive.INSTANCE, DriveSlotsState.createState(te))
  }

  override def getStateFromMeta(meta: Int): IBlockState = {
    var enumfacing = EnumFacing.getFront(meta)
    if (enumfacing.getAxis eq EnumFacing.Axis.Y) enumfacing = EnumFacing.NORTH
    this.getDefaultState.withProperty(FACING, enumfacing)
  }

  override def getMetaFromState(state: IBlockState): Int = state.getValue(FACING).getIndex

  override def withRotation(state: IBlockState, rot: Rotation): IBlockState = state.withProperty(FACING, rot.rotate(state.getValue(FACING)))


  override def withMirror(state : IBlockState, mirrorIn : Mirror) : IBlockState =
    state.withRotation(mirrorIn.toRotation(state.getValue(FACING)))

}
