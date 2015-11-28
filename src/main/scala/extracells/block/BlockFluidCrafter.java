package extracells.block;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridNode;
import buildcraft.api.tools.IToolWrench;
import extracells.network.GuiHandler;
import extracells.tileentity.TileEntityFluidCrafter;
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
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.Random;

public class BlockFluidCrafter extends BlockEC {

	IIcon icon;

	public BlockFluidCrafter() {
		super(Material.iron, 2.0F, 10.0F);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block par5,
			int par6) {
		dropItems(world, x, y, z);
		super.breakBlock(world, x, y, z, par5, par6);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFluidCrafter();
	}

	private void dropItems(World world, int x, int y, int z) {
		Random rand = new Random();

		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (!(tileEntity instanceof TileEntityFluidCrafter)) {
			return;
		}
		IInventory inventory = ((TileEntityFluidCrafter) tileEntity).inventory;

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
	public IIcon getIcon(int side, int b) {
		return this.icon;
	}

	@Override
	public String getLocalizedName() {
		return StatCollector.translateToLocal(getUnlocalizedName() + ".name");
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replace("tile.", "");
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z,
			EntityPlayer player, int side, float p_149727_7_,
			float p_149727_8_, float p_149727_9_) {
		if (world.isRemote)
			return false;
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileEntityFluidCrafter)
			if (!PermissionUtil.hasPermission(player,
					SecurityPermissions.BUILD,
					((TileEntityFluidCrafter) tile).getGridNode()))
				return false;
		ItemStack current = player.inventory.getCurrentItem();
		if (player.isSneaking() && current != null) {
			try {
				if (current.getItem() instanceof IToolWrench
						&& ((IToolWrench) current.getItem()).canWrench(player,
								x, y, z)) {
					dropBlockAsItem(world, x, y, z, new ItemStack(this));
					world.setBlockToAir(x, y, z);
					((IToolWrench) current.getItem()).wrenchUsed(player, x, y,
							z);
					return true;
				}
			} catch (Throwable e) {
				// No IToolWrench
			}
			if (current.getItem() instanceof IAEWrench
					&& ((IAEWrench) current.getItem()).canWrench(current,
							player, x, y, z)) {
				dropBlockAsItem(world, x, y, z, new ItemStack(this));
				world.setBlockToAir(x, y, z);
				return true;
			}
		}
		GuiHandler.launchGui(0, player, world, x, y, z);
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z,
			EntityLivingBase entity, ItemStack stack) {
		if (world.isRemote)
			return;
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null) {
			if (tile instanceof TileEntityFluidCrafter) {
				IGridNode node = ((TileEntityFluidCrafter) tile).getGridNode();
				if (entity != null && entity instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) entity;
					node.setPlayerID(AEApi.instance().registries().players()
							.getID(player));
				}
				node.updateState();
			}
		}
	}

	@Override
	public void onBlockPreDestroy(World world, int x, int y, int z, int meta) {
		if (world.isRemote)
			return;
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile != null) {
			if (tile instanceof TileEntityFluidCrafter) {
				IGridNode node = ((TileEntityFluidCrafter) tile).getGridNode();
				if (node != null) {
					node.destroy();
				}
			}
		}
	}

	@Override
	public void registerBlockIcons(IIconRegister iconregister) {
		this.icon = iconregister.registerIcon("extracells:fluid.crafter");
	}

}
