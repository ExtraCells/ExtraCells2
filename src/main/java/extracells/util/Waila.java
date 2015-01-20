package extracells.util;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class Waila {
	
	public static void register(IWailaRegistrar registrar){
		try{
			final IWailaDataProvider partHost = new PartWailaDataProvider();

			registrar.registerBodyProvider( partHost, IPartHost.class);
			registrar.registerNBTProvider( partHost, IPartHost.class);
		}catch(Throwable e){
			e.printStackTrace();
		}
		
	}
	
	public static void init(){
		if(Loader.isModLoaded("Waila"))
			FMLInterModComms.sendMessage( "Waila", "register", Waila.class.getName() + ".register" );
	}

}
