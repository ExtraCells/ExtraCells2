package appeng.api.me.tiles;

import net.minecraftforge.common.ForgeDirection;
import appeng.api.me.util.IGridInterface;

/**
 * Not used, Not supported, Not anything...
 */
public interface IMultiNetworkBlock
{
	void setGrid( IGridInterface gi, ForgeDirection dir );
	IGridInterface getGrid( ForgeDirection dir );
	
	boolean Propogates( ForgeDirection dir );
	
}
