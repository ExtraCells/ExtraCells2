package extracells.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import extracells.Extracells;
import extracells.tile.TileEntityLevelEmitterFluid;

public class BlockLevelEmitterFluid extends ColorableRotatableECBlock
{

	public BlockLevelEmitterFluid(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.Extracells.ModTab);
		this.setUnlocalizedName("block.fluid.levelemitter");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityLevelEmitterFluid();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (!world.isRemote)
		{
			if (world.getBlockTileEntity(x, y, z) == null || player.isSneaking())
			{
				return false;
			}
			PacketDispatcher.sendPacketToPlayer(world.getBlockTileEntity(x, y, z).getDescriptionPacket(), (Player) player);
			player.openGui(Extracells.instance, 6, world, x, y, z);
		}
		return true;
	}

	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		return side == world.getBlockMetadata(x, y, z);
	}

	public int isProvidingStrongPower(IBlockAccess block, int x, int y, int z, int side)
	{
		return ((TileEntityLevelEmitterFluid) block.getBlockTileEntity(x, y, z)).getRedstonePowerBySide(ForgeDirection.getOrientation(side));
	}

	public int isProvidingWeakPower(IBlockAccess block, int x, int y, int z, int side)
	{
		return ((TileEntityLevelEmitterFluid) block.getBlockTileEntity(x, y, z)).getRedstonePowerBySide(ForgeDirection.getOrientation(side));
	}

	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		return isProvidingStrongPower(world, x, y, z, ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z)).getOpposite().ordinal());
	}

	public boolean canProvidePower()
	{
		return true;
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
