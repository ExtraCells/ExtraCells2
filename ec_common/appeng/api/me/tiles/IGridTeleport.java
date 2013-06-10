package appeng.api.me.tiles;

import net.minecraftforge.common.ForgeDirection;

/**
 * Create a connection to a normally not connected tile - not used, possibility of changes...
 */
public interface IGridTeleport
{
	IGridTileEntity findRemoteSide( ForgeDirection dir );
}
