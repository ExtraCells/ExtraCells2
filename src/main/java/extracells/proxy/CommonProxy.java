package extracells.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.Blocks;
import appeng.api.Items;
import appeng.api.Materials;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.BlockEnum;
import extracells.Extracells;
import extracells.blocks.BlockBusFluidExport;
import extracells.blocks.BlockBusFluidImport;
import extracells.blocks.BlockBusFluidStorage;
import extracells.blocks.BlockCertusTank;
import extracells.blocks.BlockFluidTransitionPlane;
import extracells.blocks.BlockHardMEDrive;
import extracells.blocks.BlockMEBattery;
import extracells.blocks.BlockMEDropper;
import extracells.blocks.BlockSolderingStation;
import extracells.blocks.BlockTerminalFluid;
import extracells.blocks.BlockWalrusLoader;
import extracells.container.ContainerBusFluidExport;
import extracells.container.ContainerBusFluidImport;
import extracells.container.ContainerBusFluidStorage;
import extracells.container.ContainerHardMEDrive;
import extracells.container.ContainerMEBattery;
import extracells.container.ContainerTerminalFluid;
import extracells.gui.GuiBusFluidExport;
import extracells.gui.GuiBusFluidImport;
import extracells.gui.GuiBusFluidStorage;
import extracells.gui.GuiHardMEDrive;
import extracells.gui.GuiMEBattery;
import extracells.gui.GuiTerminalFluid;
import extracells.handler.CraftingHandler;
import extracells.items.ItemBlockCertusTank;
import extracells.items.ItemBlockSpecial;
import extracells.items.ItemCasing;
import extracells.items.ItemStorageComponent;
import extracells.items.ItemFluidDisplay;
import extracells.items.ItemSecureStoragePhysicalDecrypted;
import extracells.items.ItemSecureStoragePhysicalEncrypted;
import extracells.items.ItemStorageFluid;
import extracells.items.ItemStoragePhysical;
import extracells.tile.TileEntityBusFluidExport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityCertusTank;
import extracells.tile.TileEntityHardMEDrive;
import extracells.tile.TileEntityMEBattery;
import extracells.tile.TileEntityMEDropper;
import extracells.tile.TileEntitySolderingStation;
import extracells.tile.TileEntityTerminalFluid;
import extracells.tile.TileEntityTransitionPlaneFluid;
import extracells.tile.TileEntityWalrus;

public class CommonProxy implements IGuiHandler
{
	public void addRecipes()
	{
		GameRegistry.registerCraftingHandler(new CraftingHandler());

		ItemStack storagePhysical256k = new ItemStack(Extracells.StoragePhysical, 1, 0);
		ItemStack storagePhysical1m = new ItemStack(Extracells.StoragePhysical, 1, 1);
		ItemStack storagePhysical4m = new ItemStack(Extracells.StoragePhysical, 1, 2);
		ItemStack storagePhysical16m = new ItemStack(Extracells.StoragePhysical, 1, 3);

		ItemStack storageFluid1k = new ItemStack(Extracells.StorageFluid, 1, 0);
		ItemStack storageFluid4k = new ItemStack(Extracells.StorageFluid, 1, 1);
		ItemStack storageFluid16k = new ItemStack(Extracells.StorageFluid, 1, 2);
		ItemStack storageFluid64k = new ItemStack(Extracells.StorageFluid, 1, 3);

		ItemStack containerCell = new ItemStack(Extracells.StoragePhysical, 1, 4);

		ItemStack encryptableCell = new ItemStack(Extracells.StoragePhysicalDecrypted, 1, 0);

		ItemStack customCell = new ItemStack(Extracells.StoragePhysical, 1, 5);

		ItemStack physicalKilo = new ItemStack(Extracells.Cluster, 1, 0);
		ItemStack physicalMega = new ItemStack(Extracells.Cluster, 1, 1);
		ItemStack physicalGiga = new ItemStack(Extracells.Cluster, 1, 2);
		ItemStack physicalTera = new ItemStack(Extracells.Cluster, 1, 3);
		ItemStack fluidCell = new ItemStack(Extracells.Cluster, 1, 4);
		ItemStack fluidSegment = new ItemStack(Extracells.Cluster, 1, 5);
		ItemStack fluidBlock = new ItemStack(Extracells.Cluster, 1, 6);
		ItemStack fluidCluster = new ItemStack(Extracells.Cluster, 1, 7);

		ItemStack advancedStorageCasingPhysical = new ItemStack(Extracells.Casing, 1, 0);
		ItemStack advancedStorageCasingFluid = new ItemStack(Extracells.Casing, 1, 1);

		ItemStack meItemDropper = new ItemStack(BlockEnum.MEDROPPER.getBlockEntry(), 1);
		ItemStack solderingStation = new ItemStack(BlockEnum.SOLDERINGSTATION.getBlockEntry(), 1);
		ItemStack meBattery = new ItemStack(BlockEnum.MEBATTERY.getBlockEntry(), 1);
		ItemStack hardMEDrive = new ItemStack(BlockEnum.BRMEDRIVE.getBlockEntry(), 1);
		ItemStack fluidImportBus = new ItemStack(BlockEnum.FLUIDIMPORT.getBlockEntry(), 1);
		ItemStack fluidExportBus = new ItemStack(BlockEnum.FLUIDEXPORT.getBlockEntry(), 1);
		ItemStack fluidStorageBus = new ItemStack(BlockEnum.FLUIDSTORAGE.getBlockEntry(), 1);
		ItemStack fluidTerminal = new ItemStack(BlockEnum.FLUIDTERMINAL.getBlockEntry(), 1);
		ItemStack transitionPlaneFluid = new ItemStack(BlockEnum.FLUIDTRANSITION.getBlockEntry(), 1);
		ItemStack certusTank = new ItemStack(BlockEnum.CERTUSTANK.getBlockEntry(), 1);
		ItemStack chromia = new ItemStack(BlockEnum.CHROMIA.getBlockEntry(), 1);

		// Advanced Casing Physical
		GameRegistry.addShapedRecipe(advancedStorageCasingPhysical, new Object[]
		{ "GFG", "F_F", "DDD", 'G', Blocks.blkQuartzGlass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond });

		// Advanced Casing Fluid
		GameRegistry.addShapedRecipe(advancedStorageCasingFluid, new Object[]
		{ "GFG", "F_F", "TTT", 'T', certusTank, 'F', appeng.api.Materials.matFluxDust, 'G', Blocks.blkQuartzGlass });

		// Normal Cells
		GameRegistry.addShapedRecipe(storagePhysical256k, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Blocks.blkQuartzGlass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalKilo });
		GameRegistry.addShapelessRecipe(storagePhysical256k, new Object[]
		{ advancedStorageCasingPhysical, physicalKilo });
		GameRegistry.addShapedRecipe(storagePhysical1m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Blocks.blkQuartzGlass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalMega });
		GameRegistry.addShapelessRecipe(storagePhysical1m, new Object[]
		{ advancedStorageCasingPhysical, physicalMega });
		GameRegistry.addShapedRecipe(storagePhysical4m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Blocks.blkQuartzGlass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalGiga });
		GameRegistry.addShapelessRecipe(storagePhysical4m, new Object[]
		{ advancedStorageCasingPhysical, physicalGiga });
		GameRegistry.addShapedRecipe(storagePhysical16m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Blocks.blkQuartzGlass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalTera });
		GameRegistry.addShapelessRecipe(storagePhysical16m, new Object[]
		{ advancedStorageCasingPhysical, physicalTera });

		// Fluid Cells
		GameRegistry.addShapedRecipe(storageFluid1k, new Object[]
		{ "GFG", "FCF", "TTT", 'T', certusTank, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'G', Blocks.blkQuartzGlass, 'C', fluidCell });
		GameRegistry.addShapelessRecipe(storageFluid1k, new Object[]
		{ advancedStorageCasingFluid, fluidCell });
		GameRegistry.addShapedRecipe(storageFluid4k, new Object[]
		{ "GFG", "FCF", "TTT", 'T', certusTank, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'G', Blocks.blkQuartzGlass, 'C', fluidSegment });
		GameRegistry.addShapelessRecipe(storageFluid4k, new Object[]
		{ advancedStorageCasingFluid, fluidSegment });
		GameRegistry.addShapedRecipe(storageFluid16k, new Object[]
		{ "GFG", "FCF", "TTT", 'T', certusTank, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'G', Blocks.blkQuartzGlass, 'C', fluidBlock });
		GameRegistry.addShapelessRecipe(storageFluid16k, new Object[]
		{ advancedStorageCasingFluid, fluidBlock });
		GameRegistry.addShapedRecipe(storageFluid64k, new Object[]
		{ "GFG", "FCF", "TTT", 'T', certusTank, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'G', Blocks.blkQuartzGlass, 'C', fluidCluster });
		GameRegistry.addShapelessRecipe(storageFluid64k, new Object[]
		{ advancedStorageCasingFluid, fluidCluster });

		// Cell Container
		GameRegistry.addShapelessRecipe(containerCell, new Object[]
		{ appeng.api.Items.itemCell1k, Block.chest });

		// Encryptable Cell
		GameRegistry.addShapedRecipe(encryptableCell, new Object[]
		{ "_II", "ICI", "III", 'I', Item.ingotIron, 'C', Items.itemCell1k });

		// Customizable Cell
		GameRegistry.addShapedRecipe(customCell, new Object[]
		{ " P ", "SSS", " P ", 'P', appeng.api.Materials.matProcessorBasic, 'S', appeng.api.Items.itemCell1k });

		// Clusters Phsyical
		GameRegistry.addShapedRecipe(physicalKilo, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', appeng.api.Materials.matStorageCluster });
		GameRegistry.addShapedRecipe(physicalMega, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalKilo });
		GameRegistry.addShapedRecipe(physicalGiga, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalMega });
		GameRegistry.addShapedRecipe(physicalTera, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', physicalGiga });

		// Clusters Fluid
		GameRegistry.addShapedRecipe(fluidCell, new Object[]
		{ "FCF", "CPC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'C', appeng.api.Materials.matFluxCrystal });
		GameRegistry.addShapedRecipe(fluidSegment, new Object[]
		{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'P', Materials.matProcessorAdvanced, 'C', fluidCell });
		GameRegistry.addShapedRecipe(fluidBlock, new Object[]
		{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'P', Materials.matProcessorAdvanced, 'C', fluidSegment });
		GameRegistry.addShapedRecipe(fluidCluster, new Object[]
		{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'P', Materials.matProcessorAdvanced, 'C', fluidBlock });

		// SolderingStation
		GameRegistry.addShapedRecipe(solderingStation, new Object[]
		{ "III", "IDI", "I_I", 'I', Item.ingotIron, 'D', Item.diamond });

		// ME Item Dropper
		GameRegistry.addShapedRecipe(meItemDropper, new Object[]
		{ "CMC", "I_I", "IRI", 'C', Block.cobblestone, 'R', Item.redstone, 'M', appeng.api.Materials.matConversionMatrix, 'I', Item.ingotIron });

		// ME Backup Battery
		GameRegistry.addShapedRecipe(meBattery, new Object[]
		{ "EFE", "FPF", "EFE", 'E', appeng.api.Blocks.blkEnergyCell, 'F', appeng.api.Materials.matFluxCrystal, 'P', appeng.api.Materials.matProcessorBasic });

		// Blast resistant ME Drive
		GameRegistry.addShapedRecipe(hardMEDrive, new Object[]
		{ "OOO", "ODO", "OCO", 'O', Block.obsidian, 'D', appeng.api.Blocks.blkDrive, 'C', appeng.api.Blocks.blkColorlessCableCovered });

		// ME Fluid Import Bus
		GameRegistry.addShapedRecipe(fluidImportBus, new Object[]
		{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkInputCablePrecision, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });

		// ME Fluid Export Bus
		GameRegistry.addShapedRecipe(fluidExportBus, new Object[]
		{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkOutputCablePrecision, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });

		// ME Fluid Storage Bus
		GameRegistry.addShapedRecipe(fluidStorageBus, new Object[]
		{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkStorageBus, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });

		// ME Fluid Terminal
		GameRegistry.addShapedRecipe(fluidTerminal, new Object[]
		{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkTerminal, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', certusTank });

		// ME Fluid Transition Plane
		GameRegistry.addShapedRecipe(transitionPlaneFluid, new Object[]
		{ "BBB", "ITI", "ICI", 'I', Item.ingotIron, 'T', appeng.api.Blocks.blkTransitionPlane, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });

		// Certus Tank
		GameRegistry.addShapedRecipe(certusTank, new Object[]
		{ "GGG", "G_G", "GCG", 'G', Blocks.blkQuartzGlass, 'C', Blocks.blkColorlessCable, });

		// Chromia
		GameRegistry.addShapedRecipe(chromia, new Object[]
		{ "FFF", "F_F", "FFF", 'F', Item.fishRaw, });

	}

	public void RegisterTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntitySolderingStation.class, "tileEntitySolderingStation");
		GameRegistry.registerTileEntity(TileEntityMEDropper.class, "tileEntityMEDropper");
		GameRegistry.registerTileEntity(TileEntityMEBattery.class, "tileEntityMEBattery");
		GameRegistry.registerTileEntity(TileEntityHardMEDrive.class, "tileEntityHardMEDrive");
		GameRegistry.registerTileEntity(TileEntityBusFluidImport.class, "tileEntityBusFluidImport");
		GameRegistry.registerTileEntity(TileEntityBusFluidExport.class, "tileEntityBusFluidExport");
		GameRegistry.registerTileEntity(TileEntityBusFluidStorage.class, "tileEntityBusFluidStorage");
		GameRegistry.registerTileEntity(TileEntityTerminalFluid.class, "tileEntityTerminalFluid");
		GameRegistry.registerTileEntity(TileEntityTransitionPlaneFluid.class, "tileEntityTransitionPlaneFluid");
		GameRegistry.registerTileEntity(TileEntityCertusTank.class, "tileEntityCertusTank");
		GameRegistry.registerTileEntity(TileEntityWalrus.class, "tileEntityWalrus");
	}

	public void RegisterRenderers()
	{
		// Only Clientside
	}

	public void RegisterItems()
	{
		Extracells.Cluster = new ItemStorageComponent(Extracells.Cluster_ID);
		Extracells.StoragePhysical = new ItemStoragePhysical(Extracells.StoragePhysical_ID);
		Extracells.StoragePhysicalDecrypted = new ItemSecureStoragePhysicalDecrypted(Extracells.StoragePhysicalDecrypted_ID);
		Extracells.StoragePhysicalEncrypted = new ItemSecureStoragePhysicalEncrypted(Extracells.StoragePhysicalEncrypted_ID);
		Extracells.StorageFluid = new ItemStorageFluid(Extracells.StorageFluid_ID);
		Extracells.Casing = new ItemCasing(Extracells.Casing_ID);
		Extracells.FluidDisplay = new ItemFluidDisplay(Extracells.FluidDisplay_ID);
	}

	public void RegisterBlocks()
	{
		BlockEnum.SOLDERINGSTATION.setBlockEntry(new BlockSolderingStation(BlockEnum.SOLDERINGSTATION.getID()));
		GameRegistry.registerBlock(BlockEnum.SOLDERINGSTATION.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.SOLDERINGSTATION.getBlockEntry().getUnlocalizedName());
		BlockEnum.MEDROPPER.setBlockEntry(new BlockMEDropper(BlockEnum.MEDROPPER.getID()));
		GameRegistry.registerBlock(BlockEnum.MEDROPPER.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.MEDROPPER.getBlockEntry().getUnlocalizedName());
		BlockEnum.MEBATTERY.setBlockEntry(new BlockMEBattery(BlockEnum.MEBATTERY.getID()));
		GameRegistry.registerBlock(BlockEnum.MEBATTERY.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.MEBATTERY.getBlockEntry().getUnlocalizedName());
		BlockEnum.BRMEDRIVE.setBlockEntry(new BlockHardMEDrive(BlockEnum.BRMEDRIVE.getID()));
		GameRegistry.registerBlock(BlockEnum.BRMEDRIVE.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.BRMEDRIVE.getBlockEntry().getUnlocalizedName());
		BlockEnum.FLUIDIMPORT.setBlockEntry(new BlockBusFluidImport(BlockEnum.FLUIDIMPORT.getID()));
		GameRegistry.registerBlock(BlockEnum.FLUIDIMPORT.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.FLUIDIMPORT.getBlockEntry().getUnlocalizedName());
		BlockEnum.FLUIDEXPORT.setBlockEntry(new BlockBusFluidExport(BlockEnum.FLUIDEXPORT.getID()));
		GameRegistry.registerBlock(BlockEnum.FLUIDEXPORT.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.FLUIDEXPORT.getBlockEntry().getUnlocalizedName());
		BlockEnum.FLUIDSTORAGE.setBlockEntry(new BlockBusFluidStorage(BlockEnum.FLUIDSTORAGE.getID()));
		GameRegistry.registerBlock(BlockEnum.FLUIDSTORAGE.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.FLUIDSTORAGE.getBlockEntry().getUnlocalizedName());
		BlockEnum.FLUIDTERMINAL.setBlockEntry(new BlockTerminalFluid(BlockEnum.FLUIDTERMINAL.getID()));
		GameRegistry.registerBlock(BlockEnum.FLUIDTERMINAL.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.FLUIDTERMINAL.getBlockEntry().getUnlocalizedName());
		BlockEnum.FLUIDTRANSITION.setBlockEntry(new BlockFluidTransitionPlane(BlockEnum.FLUIDTRANSITION.getID()));
		GameRegistry.registerBlock(BlockEnum.FLUIDTRANSITION.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.FLUIDTRANSITION.getBlockEntry().getUnlocalizedName());
		BlockEnum.CERTUSTANK.setBlockEntry(new BlockCertusTank(BlockEnum.CERTUSTANK.getID()));
		GameRegistry.registerBlock(BlockEnum.CERTUSTANK.getBlockEntry(), ItemBlockCertusTank.class, BlockEnum.CERTUSTANK.getBlockEntry().getUnlocalizedName());
		BlockEnum.CHROMIA.setBlockEntry(new BlockWalrusLoader(BlockEnum.CHROMIA.getID()));
		GameRegistry.registerBlock(BlockEnum.CHROMIA.getBlockEntry(), ItemBlockSpecial.class, BlockEnum.CHROMIA.getBlockEntry().getUnlocalizedName());
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity != null)
		{
			switch (ID)
			{
			case 0: // GUI Hard ME Drive
				return new GuiHardMEDrive(player.inventory, (TileEntityHardMEDrive) tileEntity);
			case 1: // GUI Fluid Terminal
				return new GuiTerminalFluid(world, (TileEntityTerminalFluid) tileEntity, player.inventory);
			case 2: // GUI Storage Bus Fluid
				return new GuiBusFluidStorage(world, player.inventory, (TileEntityBusFluidStorage) tileEntity);
			case 3: // GUI Import Bus Fluid
				return new GuiBusFluidImport(world, player.inventory, (TileEntityBusFluidImport) tileEntity, player);
			case 4: // GUI Export Bus Fluid
				return new GuiBusFluidExport(world, player.inventory, (TileEntityBusFluidExport) tileEntity, player);
			case 5: // GUI ME Battery
				return new GuiMEBattery(world, (TileEntityMEBattery) tileEntity, player);
			default:
				return false;
			}
		} else
		{
			return false;
		}
	}

	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

		if (tileEntity != null)
		{
			switch (ID)
			{
			case 0: // GUI Hard ME Drive
				return new ContainerHardMEDrive(player.inventory, (TileEntityHardMEDrive) tileEntity);
			case 1: // GUI Fluid Terminal
				return new ContainerTerminalFluid(player.inventory, ((TileEntityTerminalFluid) tileEntity).getInventory());
			case 2: // GUI Storage Bus Fluid
				return new ContainerBusFluidStorage(player.inventory, ((TileEntityBusFluidStorage) tileEntity).getInventory());
			case 3: // GUI Import Bus Fluid
				return new ContainerBusFluidImport(player.inventory, ((TileEntityBusFluidImport) tileEntity).getInventory());
			case 4: // GUI Export Bus Fluid
				return new ContainerBusFluidExport(player.inventory, ((TileEntityBusFluidExport) tileEntity).getInventory());
			case 5: // GUI ME Battery
				return new ContainerMEBattery();
			default:
				return false;
			}
		} else
		{
			return false;
		}
	}

	public String getStringLocalization(String key)
	{
		return LanguageRegistry.instance().getStringLocalization(key);
	}

	public void loadLocalization(String filename, String locale)
	{
		LanguageRegistry.instance().loadLocalization(filename, locale, true);
	}
}