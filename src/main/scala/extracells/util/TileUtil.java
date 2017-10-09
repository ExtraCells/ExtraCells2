package extracells.util;

import javax.annotation.Nullable;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;

import appeng.api.AEApi;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.AEPartLocation;

public class TileUtil {

	public static void setOwner(World world, BlockPos pos, EntityLivingBase owner) {
		if (!(owner instanceof EntityPlayer)) {
			return;
		}
		setOwner(world, pos, (EntityPlayer) owner);
	}

	public static void setOwner(World world, BlockPos pos, EntityPlayer owner) {
		IGridNode node = getNode(world, pos);
		if (node == null) {
			return;
		}
		int playerID = AEApi.instance().registries().players().getID(owner);
		node.setPlayerID(playerID);
		node.updateState();
	}

	public static void destroy(World world, BlockPos pos) {
		IGridNode node = getNode(world, pos);
		if (node == null) {
			return;
		}
		node.destroy();
	}

	@Nullable
	public static IGridNode getNode(World world, BlockPos pos) {
		return getNode(world, pos, AEPartLocation.INTERNAL);
	}

	@Nullable
	public static IGridNode getNode(World world, BlockPos pos, AEPartLocation location) {
		IGridHost gridHost = getTile(world, pos, IGridHost.class);
		if (gridHost != null) {
			return gridHost.getGridNode(location);
		}
		return null;
	}

	@Nullable
	public static <T> T getTile(IBlockAccess world, BlockPos pos, Class<T> tileClass) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileClass.isInstance(tileEntity)) {
			return tileClass.cast(tileEntity);
		} else {
			return null;
		}
	}

	public interface ITileAction<T> {
		void actOnTile(T tile);
	}

	/**
	 * Performs an {@link ITileAction} on a tile if the tile exists.
	 */
	public static <T> void actOnTile(IBlockAccess world, BlockPos pos, Class<T> tileClass, ITileAction<T> tileAction) {
		T tile = getTile(world, pos, tileClass);
		if (tile != null) {
			tileAction.actOnTile(tile);
		}
	}

	@Nullable
	public static <T> T getCapability(World world, BlockPos pos, Capability<T> capability, EnumFacing facing) {
		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity != null && tileEntity.hasCapability(capability, facing)) {
			return tileEntity.getCapability(capability, facing);
		}
		return null;
	}
}
