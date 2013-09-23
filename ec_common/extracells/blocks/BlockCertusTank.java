package extracells.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import extracells.Extracells;
import extracells.tile.TileEntityCertusTank;

public class BlockCertusTank extends BlockContainer
{

	public BlockCertusTank(int id)
	{
		super(id, Material.glass);
		setCreativeTab(Extracells.ModTab);
		this.setUnlocalizedName("block.certustank");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
		setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 1.0F, 0.9375F);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityCertusTank();
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
}
