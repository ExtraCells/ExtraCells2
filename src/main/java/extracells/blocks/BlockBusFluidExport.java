package extracells.blocks;

import cpw.mods.fml.common.network.PacketDispatcher;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import appeng.api.Blocks;
import extracells.Extracells;
import extracells.tile.TileEntityBusFluidExport;

public class BlockBusFluidExport extends RotatableColorBlock
{

	public BlockBusFluidExport(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.Extracells.ModTab);
		this.setUnlocalizedName("block.fluid.bus.export");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
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

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityBusFluidExport();
	}

	public Icon getIcon(int side, int metadata)
	{
		return Blocks.blkInterface.getIconIndex();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighborID)
	{
		super.onNeighborBlockChange(world, x, y, z, neighborID);
		if (world.getBlockTileEntity(x, y, z) instanceof TileEntityBusFluidExport)
			PacketDispatcher.sendPacketToAllPlayers(world.getBlockTileEntity(x, y, z).getDescriptionPacket());
		((TileEntityBusFluidExport) world.getBlockTileEntity(x, y, z)).setRedstoneStatus(world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z));
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
			player.openGui(Extracells.instance, 4, world, x, y, z);
		}
		return true;
	}
}
