package extracells.block

import java.util

import extracells.registries.BlockEnum
import extracells.tileentity.TileEntityCertusTank
import extracells.util.PropertyTile
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.translation.{I18n, LanguageMap}
import net.minecraft.world.{IBlockAccess, World}
import net.minecraftforge.common.property.{IExtendedBlockState, IUnlistedProperty}

import scala.collection.mutable.ArrayBuffer


class BlockCertusTank extends BlockEC(Material.GLASS, 2.0F, 10.0F) with Extended{

  //setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F)

  override protected def setDefaultExtendedState(state: IBlockState) = setDefaultState(state)

  override protected def addExtendedState(state: IBlockState, world: IBlockAccess, pos: BlockPos) =
    (state, world.getTileEntity(pos)) match {
      case (extendedState: IExtendedBlockState, print: TileEntityCertusTank) =>
        super.addExtendedState(extendedState.withProperty(PropertyTile.Tile, print), world, pos)
      case _ => None
    }

  override protected def createProperties(listed: ArrayBuffer[IProperty[_ <: Comparable[_]]], unlisted: ArrayBuffer[IUnlistedProperty[_]]) {
    super.createProperties(listed, unlisted)
    unlisted += PropertyTile.Tile
  }

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = TileEntityCertusTank

    override def getDrops(world: IBlockAccess, pos: BlockPos, state: IBlockState, fortune: Int): util.List[ItemStack] = {
      val tileEntity = new NBTTagCompound
      val worldTE = world.getTileEntity(pos)
      val list = new util.ArrayList[ItemStack]
      if (worldTE != null && worldTE.isInstanceOf[TileEntityCertusTank]) {
        val dropStack = new ItemStack(BlockEnum.CERTUSTANK.getBlock, 1)
        worldTE.asInstanceOf[TileEntityCertusTank].writeToNBTWithoutCoords(tileEntity)
        if (!tileEntity.hasKey("Empty")) {
          val nbtTag = new NBTTagCompound
          nbtTag.setTag("tileEntity", tileEntity)
          dropStack.setTagCompound(nbtTag)
          list.add(dropStack)
        }

      }
      list
    }

  override def getLocalizedName: String = I18n.translateToLocal(getUnlocalizedName + ".name")

  override def getUnlocalizedName: String = super.getUnlocalizedName.replace("tile.", "")

}
