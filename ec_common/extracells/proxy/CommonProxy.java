package extracells.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.Items;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.Extracells;
import extracells.blocks.BlockBusFluidExport;
import extracells.blocks.BlockBusFluidImport;
import extracells.blocks.BlockBusFluidStorage;
import extracells.blocks.BlockHardMEDrive;
import extracells.blocks.BlockMEBattery;
import extracells.blocks.BlockMEDropper;
import extracells.blocks.BlockSolderingStation;
import extracells.blocks.BlockTerminalFluid;
import extracells.container.ContainerBusFluidExport;
import extracells.container.ContainerBusFluidImport;
import extracells.container.ContainerBusFluidStorage;
import extracells.container.ContainerHardMEDrive;
import extracells.container.ContainerTerminalFluid;
import extracells.gui.GuiBusFluidExport;
import extracells.gui.GuiBusFluidImport;
import extracells.gui.GuiBusFluidStorage;
import extracells.gui.GuiHardMEDrive;
import extracells.gui.GuiTerminalFluid;
import extracells.handler.CraftingHandler;
import extracells.items.ItemBlockSpecial;
import extracells.items.ItemCasing;
import extracells.items.ItemCluster;
import extracells.items.ItemFluidDisplay;
import extracells.items.ItemSecureStoragePhysicalDecrypted;
import extracells.items.ItemSecureStoragePhysicalEncrypted;
import extracells.items.ItemStorageFluid;
import extracells.items.ItemStoragePhysical;
import extracells.tile.TileEntityBusFluidExport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityHardMEDrive;
import extracells.tile.TileEntityMEBattery;
import extracells.tile.TileEntityMEDropper;
import extracells.tile.TileEntitySolderingStation;
import extracells.tile.TileEntityTerminalFluid;

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

		ItemStack containerCell = new ItemStack(Extracells.StoragePhysical, 1, 4);

		ItemStack encryptableCell = new ItemStack(Extracells.StoragePhysicalDecrypted, 1, 0);

		ItemStack customCell = new ItemStack(Extracells.StoragePhysical, 1, 5);

		ItemStack cluster256k = new ItemStack(Extracells.Cluster, 1, 0);
		ItemStack cluster1m = new ItemStack(Extracells.Cluster, 1, 1);
		ItemStack cluster4m = new ItemStack(Extracells.Cluster, 1, 2);
		ItemStack cluster16m = new ItemStack(Extracells.Cluster, 1, 3);

		ItemStack advancedStorageCasing = new ItemStack(Extracells.Casing, 1, 0);

		ItemStack meItemDropper = new ItemStack(Extracells.MEDropper, 1);
		ItemStack solderingStation = new ItemStack(Extracells.SolderingStation, 1);
		ItemStack meBattery = new ItemStack(Extracells.MEBattery, 1);
		ItemStack hardMEDrive = new ItemStack(Extracells.HardMEDrive, 1);
		ItemStack fluidImportBus = new ItemStack(Extracells.BusFluidImport, 1);
		ItemStack fluidExportBus = new ItemStack(Extracells.BusFluidExport, 1);
		ItemStack fluidStorageBus = new ItemStack(Extracells.BusFluidStorage, 1);
		ItemStack fluidTerminal = new ItemStack(Extracells.TerminalFluid, 1);

		// Advanced Casing
		GameRegistry.addShapedRecipe(advancedStorageCasing, new Object[]
		{ "GFG", "F_F", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond });

		// Normal Cells
		GameRegistry.addShapedRecipe(storagePhysical256k, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 0) });
		GameRegistry.addShapelessRecipe(storagePhysical256k, new Object[]
		{ Extracells.Casing, cluster256k });
		GameRegistry.addShapedRecipe(storagePhysical1m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 1) });
		GameRegistry.addShapelessRecipe(storagePhysical1m, new Object[]
		{ Extracells.Casing, cluster1m });
		GameRegistry.addShapedRecipe(storagePhysical4m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 2) });
		GameRegistry.addShapelessRecipe(storagePhysical4m, new Object[]
		{ Extracells.Casing, cluster4m });
		GameRegistry.addShapedRecipe(storagePhysical16m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 3) });
		GameRegistry.addShapelessRecipe(storagePhysical16m, new Object[]
		{ Extracells.Casing, cluster16m });

		// Fluid Cells
		GameRegistry.addShapedRecipe(storageFluid1k, new Object[]
		{ "CSC", "SBS", "CSC", 'C', appeng.api.Items.itemCell1k, 'S', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });

		// Cell Container
		GameRegistry.addShapelessRecipe(containerCell, new Object[]
		{ appeng.api.Items.itemCell1k, Block.chest });

		// Encryptable Cell
		GameRegistry.addShapedRecipe(encryptableCell, new Object[]
		{ "_II", "ICI", "III", 'I', Item.ingotIron, 'C', Items.itemCell1k });

		// Customizable Cell
		GameRegistry.addShapedRecipe(customCell, new Object[]
		{ " P ", "SSS", " P ", 'P', appeng.api.Materials.matProcessorBasic, 'S', appeng.api.Items.itemCell1k });

		// Clusters
		GameRegistry.addShapedRecipe(cluster256k, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', appeng.api.Materials.matStorageCluster });
		GameRegistry.addShapedRecipe(cluster1m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 0) });
		GameRegistry.addShapedRecipe(cluster4m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 1) });
		GameRegistry.addShapedRecipe(cluster16m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 2) });

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
		{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkTerminal, 'C', appeng.api.Blocks.blkColorlessCableCovered, 'B', Item.bucketEmpty });
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
	}

	public void RegisterRenderers()
	{
		// Only Clientside
	}

	public void RegisterItems()
	{
		Extracells.Cluster = new ItemCluster(Extracells.Cluster_ID);
		Extracells.StoragePhysical = new ItemStoragePhysical(Extracells.StoragePhysical_ID);
		Extracells.StoragePhysicalDecrypted = new ItemSecureStoragePhysicalDecrypted(Extracells.StoragePhysicalDecrypted_ID);
		Extracells.StoragePhysicalEncrypted = new ItemSecureStoragePhysicalEncrypted(Extracells.StoragePhysicalEncrypted_ID);
		Extracells.StorageFluid = new ItemStorageFluid(Extracells.StorageFluid_ID);
		Extracells.Casing = new ItemCasing(Extracells.Casing_ID);
		Extracells.FluidDisplay = new ItemFluidDisplay(Extracells.FluidDisplay_ID);
	}

	public void RegisterBlocks()
	{
		Extracells.SolderingStation = new BlockSolderingStation(Extracells.SolderingStation_ID);
		GameRegistry.registerBlock(Extracells.SolderingStation, ItemBlockSpecial.class, Extracells.SolderingStation.getUnlocalizedName());
		Extracells.MEDropper = new BlockMEDropper(Extracells.MEDropper_ID);
		GameRegistry.registerBlock(Extracells.MEDropper, ItemBlockSpecial.class, Extracells.MEDropper.getUnlocalizedName());
		Extracells.MEBattery = new BlockMEBattery(Extracells.MEBattery_ID);
		GameRegistry.registerBlock(Extracells.MEBattery, ItemBlockSpecial.class, Extracells.MEBattery.getUnlocalizedName());
		Extracells.HardMEDrive = new BlockHardMEDrive(Extracells.HardMEDrive_ID);
		GameRegistry.registerBlock(Extracells.HardMEDrive, ItemBlockSpecial.class, Extracells.HardMEDrive.getUnlocalizedName());
		Extracells.BusFluidImport = new BlockBusFluidImport(Extracells.BusFluidImport_ID);
		GameRegistry.registerBlock(Extracells.BusFluidImport, ItemBlockSpecial.class, Extracells.BusFluidImport.getUnlocalizedName());
		Extracells.BusFluidExport = new BlockBusFluidExport(Extracells.BusFluidExport_ID);
		GameRegistry.registerBlock(Extracells.BusFluidExport, ItemBlockSpecial.class, Extracells.BusFluidExport.getUnlocalizedName());
		Extracells.BusFluidStorage = new BlockBusFluidStorage(Extracells.BusFluidStorage_ID);
		GameRegistry.registerBlock(Extracells.BusFluidStorage, ItemBlockSpecial.class, Extracells.BusFluidStorage.getUnlocalizedName());
		Extracells.TerminalFluid = new BlockTerminalFluid(Extracells.TerminalFluid_ID);
		GameRegistry.registerBlock(Extracells.TerminalFluid, ItemBlockSpecial.class, Extracells.TerminalFluid.getUnlocalizedName());
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
				return new GuiTerminalFluid(x, y, z, player.inventory, tileEntity);
			case 2: // GUI Storage Bus Fluid
				return new GuiBusFluidStorage(player.inventory, (TileEntityBusFluidStorage) tileEntity);
			case 3: // GUI Import Bus Fluid
				return new GuiBusFluidImport(player.inventory, (TileEntityBusFluidImport) tileEntity);
			case 4: // GUI Export Bus Fluid
				return new GuiBusFluidExport(player.inventory, (TileEntityBusFluidExport) tileEntity);
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
				return new ContainerTerminalFluid(player.inventory, tileEntity);
			case 2: // GUI Storage Bus Fluid
				return new ContainerBusFluidStorage(player.inventory, (TileEntityBusFluidStorage) tileEntity);
			case 3: // GUI Import Bus Fluid
				return new ContainerBusFluidImport(player.inventory, (TileEntityBusFluidImport) tileEntity);
			case 4: // GUI Export Bus Fluid
				return new ContainerBusFluidExport(player.inventory, (TileEntityBusFluidExport) tileEntity);
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