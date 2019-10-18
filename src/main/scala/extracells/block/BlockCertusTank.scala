package extracells.block


/*class BlockCertusTank extends BlockEC(Material.GLASS, 2.0F, 10.0F) with Extended{

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

  override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = new TileEntityCertusTank

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

  override def getLocalizedName: String = I18n.translateToLocal(getTranslationKey + ".name")

  override def getTranslationKey: String = super.getTranslationKey.replace("tile.", "")

}*/
