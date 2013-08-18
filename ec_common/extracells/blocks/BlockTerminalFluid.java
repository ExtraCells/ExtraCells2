package extracells.blocks;

import java.text.DecimalFormat;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityMEBattery;
import extracells.tile.TileEntityTerminalFluid;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTerminalFluid extends BlockContainer
{

	@SideOnly(Side.CLIENT)
	Icon frontIcon;
	@SideOnly(Side.CLIENT)
	Icon sideIcon;
	@SideOnly(Side.CLIENT)
	Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	Icon topIcon;

	public BlockTerminalFluid(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.Extracells.ModTab);
		this.setUnlocalizedName("block.fluid.terminal");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTerminalFluid();
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (world.getBlockTileEntity(x, y, z) == null || player.isSneaking())
		{
			return false;
		}
		player.openGui(Extracells.instance, 1, world, x, y, z);
		return true;
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return giveIcon(side, 4);
	}

	@SideOnly(Side.CLIENT)
	public Icon giveIcon(int side, int metadata)
	{
		return side == metadata ? frontIcon : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity tileentity = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);

		if (tileentity != null)
		{
			return giveIcon(side, metadata);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		this.frontIcon = iconregister.registerIcon("extracells:fluid.monitor.front");
		this.sideIcon = iconregister.registerIcon("extracells:machine.side");
		this.bottomIcon = iconregister.registerIcon("extracells:machine.bottom");
		this.topIcon = iconregister.registerIcon("extracells:machine.top");
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
}
