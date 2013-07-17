package appeng.api.me.tiles;

public interface IMEPowerStorage {
	
	/**
	 * ME power stroage asset, pass the amount you want to use, and what you want to use it for.
	 * @param use
	 * @param for_what
	 * @return if you can use it.
	 */
	boolean useMEEnergy( float use, String for_what );
	
	/**
	 * Add energy to an ME Power storage.
	 * @param amt
	 * @return
	 */
	public double addMEPower( double amt );
	
	/**
	 * returns the current maximum power ( this can change :P )
	 */
	public double getMEMaxPower();
	
	/**
	 * returns the current AE Power Level
	 */
	public double getMECurrentPower();
	
}
