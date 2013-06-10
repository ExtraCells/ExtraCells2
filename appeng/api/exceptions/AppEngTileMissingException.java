package appeng.api.exceptions;

import net.minecraft.world.World;
import appeng.api.DimentionalCoord;

public class AppEngTileMissingException extends Exception {

	private static final long serialVersionUID = -3502227742711078681L;
	public DimentionalCoord dc;
	
	public AppEngTileMissingException(World w, int x, int y, int z)
	{
		dc = new DimentionalCoord( w, x, y, z );
	}
	
}
