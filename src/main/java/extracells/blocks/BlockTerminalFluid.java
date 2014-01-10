package extracells.blocks;

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
import net.minecraftforge.common.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tileentity.TileEntityTerminalFluid;

public class BlockTerminalFluid extends RotatableColorBlock
{

	@SideOnly(Side.CLIENT)
	Icon frontIcon;
	@SideOnly(Side.CLIENT)
	Icon sideIcon;
	@SideOnly(Side.CLIENT)
	Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	Icon topIcon;
	@SideOnly(Side.CLIENT)
	public Icon baseLayer;
	@SideOnly(Side.CLIENT)
	public Icon[] colorLayers;

	public BlockTerminalFluid(int id)
	{
		super(id, Material.rock);
		setCreativeTab(extracells.Extracells.ModTab);
		setUnlocalizedName("block.fluid.terminal");
		setHardness(2.0F);
		setResistance(10.0F);
	}

	@Override
	public int getRenderType()
	{
		return Extracells.renderID;
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityTerminalFluid();
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
			player.openGui(Extracells.instance, 1, world, x, y, z);
		}
		return true;
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return side == 3 ? frontIcon : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity tileentity = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);

		if (tileentity != null)
		{
			return side == metadata ? frontIcon : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		frontIcon = iconregister.registerIcon("extracells:fluid.terminal.hotbar");
		sideIcon = iconregister.registerIcon("extracells:machine.side");
		bottomIcon = iconregister.registerIcon("extracells:machine.bottom");
		topIcon = iconregister.registerIcon("extracells:machine.top");
		baseLayer = iconregister.registerIcon("extracells:fluid.terminal.layerbase");
		colorLayers = new Icon[]
		{ iconregister.registerIcon("extracells:fluid.terminal.layer3"), iconregister.registerIcon("extracells:fluid.terminal.layer2"), iconregister.registerIcon("extracells:fluid.terminal.layer1") };
	}

	@Override
	public ForgeDirection[] getValidRotations(World worldObj, int x, int y, int z)
	{
		return new ForgeDirection[]
		{ ForgeDirection.WEST, ForgeDirection.EAST, ForgeDirection.NORTH, ForgeDirection.SOUTH };
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack)
	{
		super.onBlockPlacedBy(world, x, y, z, player, itemstack);
		int l = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

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
