package extracells.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.player.EntityPlayer;

import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.parts.IPart;
import appeng.api.util.AEPartLocation;

public class PermissionUtil {

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable IGrid grid) {
		if (grid != null) {
			return hasPermission(player, permission, (ISecurityGrid) grid.getCache(ISecurityGrid.class));
		}
		return true;
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable IGridHost host) {
		return hasPermission(player, permission, host, AEPartLocation.INTERNAL);
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable IGridHost host, @Nonnull AEPartLocation side) {
		if (host != null) {
			return hasPermission(player, permission, host.getGridNode(side));
		}
		return true;
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable IGridNode host) {
		if (host != null) {
			return hasPermission(player, permission, host.getGrid());
		}
		return true;
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable Object host) {
		if (host != null && host instanceof IActionHost) {
			IActionHost actionHost = (IActionHost) host;
			return hasPermission(player, permission, actionHost.getActionableNode());
		}
		return true;
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable IPart part) {
		if (part != null) {
			return hasPermission(player, permission, part.getGridNode());
		}
		return true;
	}

	public static boolean hasPermission(@Nullable EntityPlayer player, @Nullable SecurityPermissions permission, @Nullable ISecurityGrid securityGrid) {
		if (player == null || permission == null || securityGrid == null) {
			return true;
		}
		return securityGrid.hasPermission(player, permission);
	}
}
