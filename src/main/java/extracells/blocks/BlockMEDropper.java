package extracells.blocks;

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
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import appeng.api.Util;
import appeng.api.me.items.IAEWrench;
import appeng.api.me.util.IMEInventoryHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.tileentity.TileEntityMEDropper;

public class BlockMEDropper extends RotatableColorBlock
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
		setCreativeTab(Extracells.ModTab);
		setUnlocalizedName("block.medropper");
		setHardness(2.0F);
		setResistance(10.0F);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIcon(int side, int metadata)
	{
		return side == 3 ? (metadata != 1 && metadata != 0 ? frontHorizontalIcon : frontVerticalIcon) : (metadata != 1 && metadata != 0 ? (side != 1 && side != 0 ? sideIcon : topIcon) : topIcon);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getBlockTexture(IBlockAccess blockAccess, int x, int y, int z, int side)
	{
		TileEntity tileentity = blockAccess.getBlockTileEntity(x, y, z);
		int metadata = blockAccess.getBlockMetadata(x, y, z);

		if (tileentity != null)
		{
			return side == metadata ? (metadata != 1 && metadata != 0 ? frontHorizontalIcon : frontVerticalIcon) : (metadata != 1 && metadata != 0 ? (side != 1 && side != 0 ? sideIcon : topIcon) : topIcon);
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconregister)
	{
		sideIcon = iconregister.registerIcon("extracells:machine.side");
		topIcon = iconregister.registerIcon("extracells:machine.top");
		frontHorizontalIcon = iconregister.registerIcon("extracells:medropper.front_horizontal");
		frontVerticalIcon = iconregister.registerIcon("extracells:medropper.front_vertical");
	}

	@Override
	public boolean canConnectRedstone(IBlockAccess world, int x, int y, int z, int side)
	{
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float offsetX, float offsetY, float offsetZ)
	{
		if (player.inventory.getCurrentItem() != null && player.inventory.getCurrentItem().getItem() instanceof IAEWrench)
		{
			return false;
		}
		if (!world.isRemote)
		{
			if (!player.isSneaking())
			{
				if (!((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked())
				{
					if (player.inventory.getCurrentItem() != null)
					{
						((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).setItem(player.inventory.getCurrentItem().copy());
						player.addChatMessage(StatCollector.translateToLocal("tooltip.dropset") + " " + player.inventory.getCurrentItem().getDisplayName());
					}
				} else
				{
					ItemStack item = ((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getItem();
					player.addChatMessage(StatCollector.translateToLocal("tooltip.item") + ": " + item != null ? item.getDisplayName() : StatCollector.translateToLocal("tooltip.empty1"));
				}
			} else
			{
				((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).setLocked(!((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked());
				if (((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getLocked())
				{
					player.addChatMessage(StatCollector.translateToLocal("tooltip.dropperlocked") + "!");
				} else
				{
					player.addChatMessage(StatCollector.translateToLocal("tooltip.dropperunlocked") + "!");
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
						IMEInventoryHandler cellArray = ((TileEntityMEDropper) world.getBlockTileEntity(x, y, z)).getGrid().getCellArray();
						if (cellArray != null && cellArray.extractItems(Util.createItemStack(request)) != null)
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

	protected void dispense(World world, int x, int y, int z, ItemStack toDispense)
	{
		BlockSourceImpl blockSource = new BlockSourceImpl(world, x, y, z);
		world.playAuxSFX(1001, x, y, z, 0);
		IBehaviorDispenseItem ibehaviordispenseitem = getBehaviorForItemStack(toDispense);
		ibehaviordispenseitem.dispense(blockSource, toDispense);
	}

	protected IBehaviorDispenseItem getBehaviorForItemStack(ItemStack itemstack)
	{
		return (IBehaviorDispenseItem) dispenseBehaviorRegistry.getObject(itemstack.getItem());
	}

	public static IPosition getIPositionFromBlockSource(IBlockSource par0IBlockSource)
	{
		EnumFacing enumfacing = getFacing(par0IBlockSource.getBlockMetadata());
		double d0 = par0IBlockSource.getX() + 0.7D * enumfacing.getFrontOffsetX();
		double d1 = par0IBlockSource.getY() + 0.7D * enumfacing.getFrontOffsetY();
		double d2 = par0IBlockSource.getZ() + 0.7D * enumfacing.getFrontOffsetZ();
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
