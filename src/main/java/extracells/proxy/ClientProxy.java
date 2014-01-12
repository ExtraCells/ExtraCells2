package extracells.proxy;

import cpw.mods.fml.client.registry.ClientRegistry;
import extracells.BlockEnum;
import extracells.render.item.*;
import extracells.render.tileentity.*;
import extracells.tileentity.*;
import net.minecraftforge.client.MinecraftForgeClient;

public class ClientProxy extends CommonProxy
{
	public void RegisterRenderers()
	{
		try
		{
			ClientRegistry.bindTileEntitySpecialRenderer(TileEntitySolderingStation.class, new TileEntityRendererSolderingStation());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.SOLDERINGSTATION.getBlockInstance().blockID, new ItemRendererSolderingStation());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidStorage.class, new TileEntityRendererBusFluidStorage());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDSTORAGE.getBlockInstance().blockID, new ItemRendererBusFluidStorage());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidImport.class, new TileEntityRendererBusFluidImport());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDIMPORT.getBlockInstance().blockID, new ItemRendererBusFluidImport());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityBusFluidExport.class, new TileEntityRendererBusFluidExport());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDEXPORT.getBlockInstance().blockID, new ItemRendererBusFluidExport());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCertusTank.class, new TileEntityRendererCertusTank());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.CERTUSTANK.getBlockInstance().blockID, new ItemRendererCertusTank());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityWalrus.class, new TileEntityRedererWalrus());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.CHROMIA.getBlockInstance().blockID, new ItemRendererWalrus());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityLevelEmitterFluid.class, new TileEntityRendererLevelEmitterFluid());
			MinecraftForgeClient.registerItemRenderer(BlockEnum.FLUIDLEVELEMITTER.getBlockInstance().blockID, new ItemRendererLevelEmitterFluid());

			ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMonitorStorageFluid.class, new TileEntityRendererMonitorStorageFluid());
		} catch (NullPointerException e)
		{
			System.out.println("Mod ExtraCells: Another mod probably overrid an ExtraCells item causing EC to cancel the registration of ItemRenderers!");
		}
	}
}