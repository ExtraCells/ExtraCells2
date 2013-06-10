package appeng.api;

public interface IExternalStorageRegistry {
	
	/**
	 * A registry for StorageBus interactions
	 * @param ei
	 */
	void addExternalStorageInterface( IExternalStorageHandler ei );
	
}
