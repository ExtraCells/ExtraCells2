package extracells.blocks;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import appeng.api.Blocks;
import extracells.Extracells;
import extracells.tile.TileEntityInterfaceFluid;

public class BlockInterFaceFluid extends ColorBlock
{
	@SideOnly(Side.CLIENT)
	Icon icon;

	public BlockInterFaceFluid(int id)
	{
		super(id, Material.rock);
		setCreativeTab(extracells.Extracells.ModTab);
		setUnlocalizedName("block.fluid.interface");
		setHardness(2.0F);
		setResistance(10.0F);
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
			player.openGui(Extracells.instance, 7, world, x, y, z);
		}
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityInterfaceFluid();
	}

	public Icon getIcon(int side, int metadata)
	{
		return icon;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		icon = iconregister.registerIcon("extracells:fluid.interface");
	}
}
