package extracells.util;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLInterModComms;
import extracells.tileentity.TileEntityCertusTank;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class Waila {
	
	@Optional.Method(modid = "Waila")
	public static void register(IWailaRegistrar registrar){
			final IWailaDataProvider partHost = new PartWailaDataProvider();

			registrar.registerBodyProvider( partHost, IPartHost.class);
			registrar.registerNBTProvider( partHost, IPartHost.class);
			
			final IWailaDataProvider block = new BlockWailaDataProvider();
			
			registrar.registerBodyProvider(block, TileEntityCertusTank.class);
			registrar.registerNBTProvider(block, TileEntityCertusTank.class);
		
	}
	
	public static void init(){
		if(Loader.isModLoaded("Waila"))
			FMLInterModComms.sendMessage( "Waila", "register", Waila.class.getName() + ".register" );
	}

}
