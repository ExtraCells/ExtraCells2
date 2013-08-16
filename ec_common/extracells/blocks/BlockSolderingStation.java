package extracells.blocks;

import java.util.Random;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import cpw.mods.fml.common.FMLCommonHandler;
import extracells.Extracells;
import extracells.gui.GuiSolderingStation;
import extracells.tile.TileEntitySolderingStation;

public class BlockSolderingStation extends BlockContainer
{

	public BlockSolderingStation(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(Extracells.ModTab);
		this.setUnlocalizedName("block.solderingstation");
		this.setHardness(2.0F);
		this.setResistance(6000F);
		this.setLightValue(0.02f);
		this.setBlockBounds(0F, 0F, 0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public int quantityDropped(Random par1Random)
	{
		return 1;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack)
	{
		int l = MathHelper.floor_double((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

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
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
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
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntitySolderingStation();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int i, float a, float b, float c)
	{
		if (!world.isRemote)
			((TileEntitySolderingStation) world.getBlockTileEntity(x, y, z)).addUser(player.username);
		if (world.isRemote)
		{
			FMLCommonHandler.instance().showGuiScreen(new GuiSolderingStation(x, y, z, (player.getHeldItem() != null && player.getHeldItem().getItem() == Extracells.StoragePhysical && player.getHeldItem().getItemDamage() == 5)));
			return true;
		} else
		{
			return true;
		}
	}
}