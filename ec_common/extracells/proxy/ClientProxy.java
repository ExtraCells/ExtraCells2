package extracells.proxy;

import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import extracells.extracells;
import extracells.model.render.ItemSolderingStationRenderer;
import extracells.model.render.TileEntitySolderingStationRenderer;
import extracells.tile.TileEntitySolderingStation;

public class ClientProxy extends CommonProxy{
	public void RegisterRenderers(){
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolderingStation.class, new TileEntitySolderingStationRenderer());
	    MinecraftForgeClient.registerItemRenderer(extracells.SolderingStation.blockID, new ItemSolderingStationRenderer());
	}
}