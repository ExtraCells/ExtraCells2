package appeng.api.exceptions;

import cpw.mods.fml.common.FMLLog;

public class AppEngSyncIssue extends Exception {

	private static final long serialVersionUID = -9051434206368465493L;
	private Thread tar;
	
	public AppEngSyncIssue( Thread target )
    {
        super( "Multithreading invalidation" );
        tar = target;
    }
	
	@Override
	public void printStackTrace() {
		FMLLog.severe("A non threadsafe component was accessed another thread was accessed by a diffrent thread.");
		FMLLog.severe("Expected From: " + tar.getName() );
		FMLLog.severe("Accessed From: " + Thread.currentThread().getName() );
		super.printStackTrace();
	}
	
}
