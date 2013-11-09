package extracells.proxy;

import net.minecraftforge.client.MinecraftForgeClient;
import cpw.mods.fml.client.registry.ClientRegistry;
import extracells.BlockEnum;
import extracells.model.render.item.ItemRendererBusFluidExport;
import extracells.model.render.item.ItemRendererBusFluidImport;
import extracells.model.render.item.ItemRendererBusFluidStorage;
import extracells.model.render.item.ItemRendererCertusTank;
import extracells.model.render.item.ItemRendererLevelEmitterFluid;
import extracells.model.render.item.ItemRendererSolderingStation;
import extracells.model.render.item.ItemRendererWalrus;
import extracells.model.render.tileentity.TileEntityRedererWalrus;
import extracells.model.render.tileentity.TileEntityRendererBusFluidExport;
import extracells.model.render.tileentity.TileEntityRendererBusFluidImport;
import extracells.model.render.tileentity.TileEntityRendererBusFluidStorage;
import extracells.model.render.tileentity.TileEntityRendererCertusTank;
import extracells.model.render.tileentity.TileEntityRendererLevelEmitterFluid;
import extracells.model.render.tileentity.TileEntityRendererSolderingStation;
import extracells.tile.TileEntityBusFluidExport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityCertusTank;
import extracells.tile.TileEntityLevelEmitterFluid;
import extracells.tile.TileEntitySolderingStation;
import extracells.tile.TileEntityWalrus;

public class ClientProxy extends CommonProxy
{
	public void RegisterRenderers()
	{
		try
		{
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolderingStation.class, new TileEntityRendererSolderingStation());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.SOLDERINGSTATION.getBlockEntry().blockID, new ItemRendererSolderingStation());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidStorage.class, new TileEntityRendererBusFluidStorage());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDSTORAGE.getBlockEntry().blockID, new ItemRendererBusFluidStorage());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidImport.class, new TileEntityRendererBusFluidImport());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDIMPORT.getBlockEntry().blockID, new ItemRendererBusFluidImport());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidExport.class, new TileEntityRendererBusFluidExport());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDEXPORT.getBlockEntry().blockID, new ItemRendererBusFluidExport());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCertusTank.class, new TileEntityRendererCertusTank());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.CERTUSTANK.getBlockEntry().blockID, new ItemRendererCertusTank());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRedererWalrus());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.CHROMIA.getBlockEntry().blockID, new ItemRendererWalrus());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLevelEmitterFluid.class, new TileEntityRendererLevelEmitterFluid());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDLEVELEMITTER.getBlockEntry().blockID, new ItemRendererLevelEmitterFluid());
		} catch (NullPointerException e)
		{
		}
	}
}