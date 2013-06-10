package appeng.api.me.tiles;

public interface IMEPowerStorage {
	
	/**
	 * ME power stroage asset, pass the amount you want to use, and what you want to use it for.
	 * @param use
	 * @param for_what
	 * @return if you can use it.
	 */
	boolean useMEEnergy( float use, String for_what );
	
}
