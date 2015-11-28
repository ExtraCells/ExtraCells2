package extracells.block;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridNode;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.api.IECTileEntity;
import extracells.network.GuiHandler;
import extracells.tileentity.IListenerTile;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.util.PermissionUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.Random;

public class ECBaseBlock extends BlockEC {

	private IIcon[] icons = new IIcon[2];

	public ECBaseBlock() {
		super(Material.iron, 2.0F, 10.0F);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5,
			int par6) {
		dropPatter(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		switch (meta) {
		case 0:
			return new TileEntityFluidInterface();
		case 1:
			return new TileEntityFluidFiller();
		default:
			return null;
		}

	}

	@Override
	public int damageDropped(int p_149692_1_) {
		return p_149692_1_;
	}

	private void dropPatter(World world, int x, int y, int z) {
		Random rand = new Random();

		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityFluidInterface)) {
			return;
		}
		IInventory inventory = ((TileEntityFluidInterface) tileEntity).inventory;

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);

			if (item != null && item.stackSize > 0) {
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityItem = new EntityItem(world, x + rx, y + ry, z
						+ rz, item.copy());

				if (item.hasTagCompound()) {
					entityItem.getEntityItem().setTagCompound(
							(NBTTagCompound) item.getTagCompound().copy());
				}

				float factor = 0.05F;
				entityItem.motionX = rand.nextGaussian() * factor;
				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
				entityItem.motionZ = rand.nextGaussian() * factor;
				world.spawnEntityInWorld(entityItem);
				item.stackSize = 0;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if (meta >= 0 && meta + 1 <= this.icons.length) {
			return this.icons[meta];
		}
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int side, float p_149727_7_,
			float p_149727_8_, float p_149727_9_) {
		if (world.isRemote)
			return false;
		Random rand = new Random();
		switch (world.getBlockMetadata(x, y, z)) {
		case 0:
		case 1:
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof IECTileEntity)
				if (!PermissionUtil.hasPermission(player,
						SecurityPermissions.BUILD, ((IECTileEntity) tile)
								.getGridNode(ForgeDirection.UNKNOWN)))
					return false;
			ItemStack current = player.getCurrentEquippedItem();
			if (player.isSneaking() && current != null) {
				try {
					if (current.getItem() instanceof IToolWrench
							&& ((IToolWrench) current.getItem()).canWrench(
									player, x, y, z)) {
						ItemStack block = new ItemStack(this, 1,
								world.getBlockMetadata(x, y, z));
						if (tile != null
								&& tile instanceof TileEntityFluidInterface) {
							block.setTagCompound(((TileEntityFluidInterface) tile)
									.writeFilter(new NBTTagCompound()));
						}
						dropBlockAsItem(world, x, y, z, block);
						world.setBlockToAir(x, y, z);
						((IToolWrench) current.getItem()).wrenchUsed(player, x,
								y, z);
						return true;
					}
				} catch (Throwable e) {
					// No IToolWrench
				}
				if (current.getItem() instanceof IAEWrench
						&& ((IAEWrench) current.getItem()).canWrench(current,
								player, x, y, z)) {
					ItemStack block = new ItemStack(this, 1,
							world.getBlockMetadata(x, y, z));;
					if (tile != null
							&& tile instanceof TileEntityFluidInterface) {
						block.setTagCompound(((TileEntityFluidInterface) tile)
								.writeFilter(new NBTTagCompound()));
					}
					dropBlockAsItem(world, x, y, z, block);
					world.setBlockToAir(x, y, z);
					return true;
				}

			}
			GuiHandler.launchGui(0, player, world, x, y, z);
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z,
			EntityLivingBase entity, ItemStack stack) {
		if (world.isRemote)
			return;
		switch (world.getBlockMetadata(x, y, z)) {
		case 0:
		case 1:
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null) {
				if (tile instanceof IECTileEntity) {
					IGridNode node = ((IECTileEntity) tile)
							.getGridNode(ForgeDirection.UNKNOWN);
					if (entity != null && entity instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) entity;
						node.setPlayerID(AEApi.instance().registries()
								.players().getID(player));
					}
					node.updateState();
				}
				if (tile instanceof IListenerTile)
					((IListenerTile) tile).registerListener();
			}
			return;
		default:
			return;
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if (world.isRemote)
			return;
		switch (meta) {
		case 0:
		case 1:
			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile != null) {
				if (tile instanceof IECTileEntity) {
					IGridNode node = ((IECTileEntity) tile)
							.getGridNode(ForgeDirection.UNKNOWN);
					if (node != null) {
						node.destroy();
					}
				}
				if (tile instanceof IListenerTile)
					((IListenerTile) tile).removeListener();
			}
			return;
		default:
			return;
		}

	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		this.icons[0] = register.registerIcon("extracells:fluid.interface");
		this.icons[1] = register.registerIcon("extracells:fluid.filler");
	}
}
