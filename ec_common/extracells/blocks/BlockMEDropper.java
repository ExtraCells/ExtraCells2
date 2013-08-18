package extracells.blocks;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockSourceImpl;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.IRegistry;
import net.minecraft.dispenser.PositionImpl;
import net.minecraft.dispenser.RegistryDefaulted;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.Util;
import appeng.api.WorldCoord;
import appeng.api.events.GridTileConnectivityEvent;
import appeng.api.events.GridTileLoadEvent;
import appeng.api.events.GridTileUnloadEvent;
import extracells.Extracells;
import extracells.tile.TileEntityMEDropper;

public class BlockMEDropper extends BlockContainer
{
	public static final IRegistry dispenseBehaviorRegistry = new RegistryDefaulted(new BehaviorDefaultDispenseItem());
	@SideOnly(Side.CLIENT)
	public Icon sideIcon;
	@SideOnly(Side.CLIENT)
	public Icon topIcon;
	@SideOnly(Side.CLIENT)
	public Icon frontHorizontalIcon;
	@SideOnly(Side.CLIENT)
	public Icon frontVerticalIcon;
	public Boolean unpowered = true;

	public BlockMEDropper(int id)
	{
		super(id, Material.rock);
		this.setCreativeTab(Extracells.ModTab);
		this.setUnlocalizedName("block.medropper");
		this.setHardness(2.0F);
		this.setResistance(10.0F);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return giveIcon(side, 3);
	}

	@SideOnly(Side.CLIENT)
	public Icon giveIcon(int side, int metadata)
	{
		return side == metadata ? (metadata != 1 && metadata != 0 ? this.frontHorizontalIcon : this.frontVerticalIcon) : (metadata != 1 && metadata != 0 ? (side != 1 && side != 0 ? this.sideIcon : this.topIcon) : this.topIcon);
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
		this.sideIcon = iconregister.registerIcon("extracells:machine.side");
		this.topIcon = iconregister.registerIcon("extracells:machine.top");
		this.frontHorizontalIcon = iconregister.registerIcon("extracells:medropper.front_horizontal");
		this.frontVerticalIcon = iconregister.registerIcon("extracells:medropper.front_vertical");
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (!world.isRemote)
		{
			if (!p.isSneaking())
			{
				if (!((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked())
				{
					if (p.inventory.getCurrentItem() != null)
					{
						((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).setItem(p.inventory.getCurrentItem().copy());
						p.addChatMessage(StatCollector.translateToLocal("tooltip.dropset") + " " + p.inventory.getCurrentItem().getDisplayName());
					}
				} else
				{
					p.addChatMessage(StatCollector.translateToLocal("tooltip.item") + ": " + ((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getItem().getDisplayName());
				}
			} else
			{
				((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).setLocked(!((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked());
				if (((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked())
				{
					p.addChatMessage(StatCollector.translateToLocal("tooltip.dropperlocked") + "!");
				} else
				{
					p.addChatMessage(StatCollector.translateToLocal("tooltip.dropperunlocked") + "!");
				}
			}
		}
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, int neighbourID)
	{
		if (((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).todispense != null)
		{
			ItemStack request = ((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).todispense;
			if (!world.isRemote)
			{
				if ((world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z)) && unpowered)
				{
					if (((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getGrid() != null)
					{
						if (((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getGrid().getCellArray().extractItems(Util.createItemStack(request)) != null)
						{
							dispense(world, x, y, z, ((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getItem().copy());
							unpowered = false;
						}
					}
				} else
				{
					unpowered = true;
				}
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemstack)
	{
		int l = BlockPistonBase.determineOrientation(world, x, y, z, player);
		world.setBlockMetadataWithNotify(x, y, z, l, 2);
	}

	protected void dispense(World world, int x, int y, int z, ItemStack toDispense)
	{
		BlockSourceImpl blockSource = new BlockSourceImpl(world, x, y, z);
		world.playAuxSFX(1001, x, y, z, 0);
		IBehaviorDispenseItem ibehaviordispenseitem = this.getBehaviorForItemStack(toDispense);
		ibehaviordispenseitem.dispense(blockSource, toDispense);
	}

	protected IBehaviorDispenseItem getBehaviorForItemStack(ItemStack itemstack)
	{
		return (IBehaviorDispenseItem) dispenseBehaviorRegistry.getObject(itemstack.getItem());
	}

	public static IPosition getIPositionFromBlockSource(IBlockSource par0IBlockSource)
	{
		EnumFacing enumfacing = getFacing(par0IBlockSource.getBlockMetadata());
		double d0 = par0IBlockSource.getX() + 0.7D * (double) enumfacing.getFrontOffsetX();
		double d1 = par0IBlockSource.getY() + 0.7D * (double) enumfacing.getFrontOffsetY();
		double d2 = par0IBlockSource.getZ() + 0.7D * (double) enumfacing.getFrontOffsetZ();
		return new PositionImpl(d0, d1, d2);
	}

	public static EnumFacing getFacing(int metadata)
	{
		return EnumFacing.getFront(metadata);
	}

	@Override
	public TileEntity createNewTileEntity(World world)
	{
		return new TileEntityMEDropper();
	}

	@Override
	public boolean hasTileEntity()
	{
		return true;
	}
}
