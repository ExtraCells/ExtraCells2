package extracells.blocks;

import appeng.api.events.GridTileLoadEvent;
import appeng.api.me.items.IAEWrench;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityMEBattery;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;

public class BlockBusFluidImport extends BlockRotatable
{

	@SideOnly(Side.CLIENT)
	Icon frontIcon;
	@SideOnly(Side.CLIENT)
	Icon sideIcon;
	@SideOnly(Side.CLIENT)
	Icon bottomIcon;
	@SideOnly(Side.CLIENT)
	Icon topIcon;

	public BlockBusFluidImport(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(extracells.Extracells.ModTab);
		this.setUnlocalizedName("block.fluid.bus.import");
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
		return new TileEntityBusFluidImport();
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return giveIcon(side, 3);
	}

	@SideOnly(Side.CLIENT)
	public Icon giveIcon(int side, int metadata)
	{
		return side == metadata ? frontIcon : side == 0 ? bottomIcon : side == 1 ? topIcon : sideIcon;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighbourID)
	{
		PacketDispatcher.sendPacketToAllPlayers(world.getBlockTileEntity(x, y, z).getDescriptionPacket());
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

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof IAEWrench)
		{
			return false;
		}
		if (world.getBlockTileEntity(x, y, z) == null || player.isSneaking())
		{
			return false;
		}
		player.openGui(Extracells.instance, 3, world, x, y, z);
		return true;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		this.frontIcon = iconregister.registerIcon("extracells:fluid.bus.import.front");
		this.sideIcon = iconregister.registerIcon("extracells:machine.side");
		this.bottomIcon = iconregister.registerIcon("extracells:machine.bottom");
		this.topIcon = iconregister.registerIcon("extracells:machine.top");
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack)
	{
		if (player.isSneaking())
		{
			world.setBlockMetadataWithNotify(x, y, z, ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z)).getOpposite().ordinal(), 3);
		}
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hiZ, int meta)
	{

		return side;
	}
}
