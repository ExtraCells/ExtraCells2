package extracells.integration.waila;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.common.event.FMLInterModComms;
import extracells.tileentity.TileEntityCertusTank;
import mcp.mobius.waila.api.IWailaDataProvider;
import mcp.mobius.waila.api.IWailaRegistrar;

public class Waila {

	public static void init() {
		FMLInterModComms.sendMessage("Waila", "register", Waila.class.getName() + ".register");
	}

	@Optional.Method(modid = "Waila")
	public static void register(IWailaRegistrar registrar) {
		final IWailaDataProvider partHost = new PartWailaDataProvider();

		registrar.registerBodyProvider(partHost, IPartHost.class);
		registrar.registerNBTProvider(partHost, IPartHost.class);

		final IWailaDataProvider tileCertusTank = new TileCertusTankWailaDataProvider();

		registrar.registerBodyProvider(tileCertusTank,
				TileEntityCertusTank.class);
		registrar.registerNBTProvider(tileCertusTank,
				TileEntityCertusTank.class);

		final IWailaDataProvider blocks = new BlockWailaDataProvider();

		registrar.registerBodyProvider(blocks, IWailaTile.class);
		registrar.registerNBTProvider(blocks, IWailaTile.class);
	}

}
