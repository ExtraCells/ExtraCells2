package extracells.blocks;

import java.lang.reflect.Method;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.me.tiles.IColoredMETile;

public abstract class RotatableColorBlock extends ColorBlock
{

	public RotatableColorBlock(int id, Material material)
	{
		super(id, material);
		isBlockContainer = true;
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

	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hiZ, int meta)
	{
		return side;
	}
}
