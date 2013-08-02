package extracells.proxy;

import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import extracells.Extracells;
import extracells.model.render.ItemSolderingStationRenderer;
import extracells.model.render.TileEntitySolderingStationRenderer;
import extracells.tile.TileEntitySolderingStation;

public class ClientProxy extends CommonProxy
{
	public void RegisterRenderers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolderingStation.class, new TileEntitySolderingStationRenderer());
		MinecraftForgeClient.registerItemRenderer(Extracells.SolderingStation.blockID, new ItemSolderingStationRenderer());
	}
}