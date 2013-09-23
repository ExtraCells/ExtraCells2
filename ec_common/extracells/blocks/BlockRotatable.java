package extracells.blocks;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.RotationHelper;

public abstract class BlockRotatable extends BlockContainer
{

	public BlockRotatable(int id, Material material)
	{
		super(id, material);
		isBlockContainer = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return null;
	}

	@Override
	public boolean rotateBlock(World worldObj, int x, int y, int z, ForgeDirection axis)
	{
		Boolean validDirection = isInValidRotations(worldObj, x, y, z, axis);

		if (ForgeDirection.getOrientation(worldObj.getBlockMetadata(x, y, z)) == axis && !worldObj.isRemote)
		{
			worldObj.destroyBlock(x, y, z, true);
		} else if (validDirection)
		{
			worldObj.setBlockMetadataWithNotify(x, y, z, axis.ordinal(), 3);
		}
		return true;
	}

	public boolean isInValidRotations(World worldObj, int x, int y, int z, ForgeDirection toCheck)
	{
		for (ForgeDirection currentDirection : getValidRotations(worldObj, x, y, z))
		{
			if (currentDirection == toCheck)
				return true;
		}
		return false;
	}

	public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
	{
		return ForgeDirection.VALID_DIRECTIONS;
	}
}
