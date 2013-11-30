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

public abstract class ColorBlock extends BlockContainer
{

	public ColorBlock(int id, Material material)
	{
		super(id, material);
		isBlockContainer = true;
	}

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack)
	{
		ForgeDirection orientation = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));

		TileEntity clickedOnTileEntity = world.getBlockTileEntity(x + orientation.getOpposite().offsetX, y + orientation.getOpposite().offsetY, z + orientation.getOpposite().offsetZ);
		TileEntity blockTileEntity = world.getBlockTileEntity(x, y, z);

		if (blockTileEntity instanceof IColoredMETile)
		{
			if (clickedOnTileEntity instanceof IColoredMETile)
			{

				// All this shit to make it compatible w/ AE 13 AND 14 :D
				Method isColoredMethod = null;
				boolean isColored = false;

				try
				{
					// Method for AE 14 (With forgeDireciton argument)
					isColoredMethod = ((IColoredMETile) clickedOnTileEntity).getClass().getDeclaredMethod("isColored", ForgeDirection.class);
					isColored = (Boolean) isColoredMethod.invoke((IColoredMETile) clickedOnTileEntity, orientation);
				} catch (Throwable ex)
				{
					try
					{
						// Method for AE 13 (W/O the argument)
						isColoredMethod = ((IColoredMETile) clickedOnTileEntity).getClass().getDeclaredMethod("isColored");
						isColored = (Boolean) isColoredMethod.invoke((IColoredMETile) clickedOnTileEntity);
					} catch (Throwable e)
					{
					}
				}

				if (isColored)
				{
					((IColoredMETile) blockTileEntity).setColor(((IColoredMETile) clickedOnTileEntity).getColor());
				} else
				{
					((IColoredMETile) blockTileEntity).setColor(-1);
				}
			} else
			{
				((IColoredMETile) blockTileEntity).setColor(-1);
			}
		}

		if (player.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, orientation.getOpposite().ordinal(), 3);
		}
	}
}
