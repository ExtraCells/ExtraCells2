package extracells.block

import java.util.Random

import appeng.api.AEApi
import appeng.api.config.SecurityPermissions
import appeng.api.implementations.items.IAEWrench
import appeng.api.networking.IGridNode
import buildcraft.api.tools.IToolWrench
import cpw.mods.fml.relauncher.{Side, SideOnly}
import extracells.container.ContainerHardMEDrive
import extracells.gui.GuiHardMEDrive
import extracells.network.GuiHandler
import extracells.render.block.RendererHardMEDrive
import extracells.tileentity.TileEntityHardMeDrive
import extracells.util.PermissionUtil
import net.minecraft.block.Block
import net.minecraft.client.renderer.texture.IIconRegister
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{IIcon, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.common.util.ForgeDirection


object BlockHardMEDrive extends BlockEC(net.minecraft.block.material.Material.rock, 2.0F, 1000000.0F) with TGuiBlock{


  var frontIcon: IIcon = null
  var sideIcon: IIcon = null
  var bottomIcon: IIcon = null
  var topIcon: IIcon = null

  @SideOnly(Side.CLIENT)
  override def getClientGuiElement(player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any = {
    val tile = world.getTileEntity(x, y, z)
    if (tile == null || player == null) return null
    tile match {
      case tileMe: TileEntityHardMeDrive => {
        return new GuiHardMEDrive(player.inventory, tileMe)
      }
      case _ => return null
    }
  }

  override def getServerGuiElement(player: EntityPlayer, world: World, x: Int, y: Int, z: Int): Any = {
    val tile = world.getTileEntity(x, y, z)
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

  setBlockName("block.hardmedrive");

  override def createNewTileEntity(world : World, meta : Int): TileEntity = new TileEntityHardMeDrive()

  override def breakBlock(world: World, x: Int, y: Int, z: Int, block: Block, par6: Int) {
    dropItems(world, x, y, z)
    super.breakBlock(world, x, y, z, block, par6)
  }

  private def dropItems(world: World, x: Int, y: Int, z: Int) {
    val rand: Random = new Random
    val tileEntity: TileEntity = world.getTileEntity(x, y, z)
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

  override def onBlockActivated(world: World, x: Int, y: Int, z: Int, player: EntityPlayer, side: Int, p_149727_7_ : Float, p_149727_8_ : Float, p_149727_9_ : Float): Boolean = {
    if (world.isRemote) return false
    val tile: TileEntity = world.getTileEntity(x, y, z)
    if (tile.isInstanceOf[TileEntityHardMeDrive]) if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(ForgeDirection.UNKNOWN))) return false
    val current: ItemStack = player.inventory.getCurrentItem
    if (player.isSneaking && current != null) {
      try {
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
      }
      if (current.getItem.isInstanceOf[IAEWrench] && (current.getItem.asInstanceOf[IAEWrench]).canWrench(current, player, x, y, z)) {
        dropBlockAsItem(world, x, y, z, new ItemStack(this))
        world.setBlockToAir(x, y, z)
        return true
      }
    }
    GuiHandler.launchGui(0, player, world, x, y, z)
    return true
  }

  override def onBlockPlacedBy(world: World, x: Int, y: Int, z: Int, entity: EntityLivingBase, stack: ItemStack) {
    super.onBlockPlacedBy(world, x, y, z, entity, stack);
    val l = MathHelper.floor_double(entity.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

    if (!entity.isSneaking())
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
    }
    if (world.isRemote) return
    val tile: TileEntity = world.getTileEntity(x, y, z)
    if (tile != null) {
      if (tile.isInstanceOf[TileEntityHardMeDrive]) {
        val node: IGridNode = (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(ForgeDirection.UNKNOWN)
        if (entity != null && entity.isInstanceOf[EntityPlayer]) {
          val player: EntityPlayer = entity.asInstanceOf[EntityPlayer]
          node.setPlayerID(AEApi.instance.registries.players.getID(player))
        }
        node.updateState
      }
    }
  }

  override def onBlockPreDestroy(world: World, x: Int, y: Int, z: Int, meta: Int) {
    if (world.isRemote) return
    val tile: TileEntity = world.getTileEntity(x, y, z)
    if (tile != null) {
      if (tile.isInstanceOf[TileEntityHardMeDrive]) {
        val node: IGridNode = (tile.asInstanceOf[TileEntityHardMeDrive]).getGridNode(ForgeDirection.UNKNOWN)
        if (node != null) {
          node.destroy
        }
      }
    }
  }

  @SideOnly(Side.CLIENT)
  override def getIcon(side: Int, metadata: Int) = {
    if(side == metadata)
      frontIcon
    else if(side == 0)
      bottomIcon
    else if(side == 1)
      topIcon
    else
      sideIcon
  }

  @SideOnly(Side.CLIENT)
  override def registerBlockIcons(register: IIconRegister) = {
    frontIcon = register.registerIcon("extracells:hardmedrive.face");
    sideIcon = register.registerIcon("extracells:hardmedrive.side");
    bottomIcon = register.registerIcon("extracells:machine.bottom");
    topIcon = register.registerIcon("extracells:machine.top");
  }

  override def getRenderType : Int = RendererHardMEDrive.getRenderId

}
