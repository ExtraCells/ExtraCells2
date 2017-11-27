package extracells.block;

import javax.annotation.Nullable;
import java.util.Random;

import extracells.api.IWrenchHandler;
import extracells.util.WrenchUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IAEWrench;
import extracells.network.GuiHandler;
import extracells.tileentity.TileEntityFluidCrafter;
import extracells.util.PermissionUtil;
import extracells.util.TileUtil;

public class BlockFluidCrafter extends BlockEC {

	public BlockFluidCrafter() {
		super(Material.IRON, 2.0F, 10.0F);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		dropItems(world, pos);
		if (!world.isRemote)
			TileUtil.destroy(world, pos);
		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFluidCrafter();
	}

	private void dropItems(World world, BlockPos pos) {
		Random rand = new Random();

		TileEntity tileEntity = world.getTileEntity(pos);
		if (!(tileEntity instanceof TileEntityFluidCrafter)) {
			return;
		}
		IInventory inventory = ((TileEntityFluidCrafter) tileEntity).inventory;

		for (int i = 0; i < inventory.getSizeInventory(); i++) {
			ItemStack item = inventory.getStackInSlot(i);

			if (item != null && item.getCount() > 0) {
				float rx = rand.nextFloat() * 0.8F + 0.1F;
				float ry = rand.nextFloat() * 0.8F + 0.1F;
				float rz = rand.nextFloat() * 0.8F + 0.1F;

				EntityItem entityItem = new EntityItem(world, pos.getX() + rx, pos.getY() + ry, pos.getZ()
					+ rz, item.copy());

				if (item.hasTagCompound()) {
					entityItem.getItem().setTagCompound(
						item.getTagCompound().copy());
				}

				float factor = 0.05F;
				entityItem.motionX = rand.nextGaussian() * factor;
				entityItem.motionY = rand.nextGaussian() * factor + 0.2F;
				entityItem.motionZ = rand.nextGaussian() * factor;
				world.spawnEntity(entityItem);
				item.setCount(0);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}

		ItemStack current = player.getHeldItem(hand);
		if (!player.isSneaking()) {
			TileEntity tile = world.getTileEntity(pos);
			if (!PermissionUtil.hasPermission(player, SecurityPermissions.BUILD, tile)) {
				return false;
			}
			RayTraceResult rayTraceResult = new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos);
			IWrenchHandler wrenchHandler = WrenchUtil.getHandler(current, player, rayTraceResult, hand);
			if (wrenchHandler != null) {
				spawnAsEntity(world, pos, new ItemStack(this));
				world.setBlockToAir(pos);
				wrenchHandler.wrenchUsed(current, player, rayTraceResult, hand);
				return true;
			}
		}

		GuiHandler.launchGui(0, player, world, pos.getX(), pos.getY(), pos.getZ());
		return true;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		if (world.isRemote) {
			return;
		}
		TileUtil.setOwner(world, pos, placer);
	}

}
