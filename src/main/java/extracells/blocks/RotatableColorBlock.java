package extracells.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileConnectivityEvent;
import appeng.api.me.tiles.IGridTileEntity;

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
			TileEntity te = worldObj.getBlockTileEntity(x, y, z);
			if (te instanceof IGridTileEntity)
			{
				MinecraftForge.EVENT_BUS.post(new GridTileConnectivityEvent((IGridTileEntity) te, worldObj, new WorldCoord(x, y, z)));
			}
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
