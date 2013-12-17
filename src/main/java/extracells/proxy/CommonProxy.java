package extracells.proxy;

import java.util.logging.Level;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import appeng.api.Blocks;
import appeng.api.Items;
import appeng.api.Materials;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.BlockEnum;
import extracells.ItemEnum;
import extracells.container.ContainerBusFluidExport;
import extracells.container.ContainerBusFluidImport;
import extracells.container.ContainerBusFluidStorage;
import extracells.container.ContainerFluidCrafter;
import extracells.container.ContainerHardMEDrive;
import extracells.container.ContainerInterfaceFluid;
import extracells.container.ContainerLevelEmitterFluid;
import extracells.container.ContainerMEBattery;
import extracells.container.ContainerTerminalFluid;
import extracells.gui.GuiBusFluidExport;
import extracells.gui.GuiBusFluidImport;
import extracells.gui.GuiBusFluidStorage;
import extracells.gui.GuiFluidCrafter;
import extracells.gui.GuiHardMEDrive;
import extracells.gui.GuiInterfaceFluid;
import extracells.gui.GuiLevelEmitterFluid;
import extracells.gui.GuiMEBattery;
import extracells.gui.GuiTerminalFluid;
import extracells.tile.TileEntityBusFluidExport;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityCertusTank;
import extracells.tile.TileEntityFluidCrafter;
import extracells.tile.TileEntityHardMEDrive;
import extracells.tile.TileEntityInterfaceFluid;
import extracells.tile.TileEntityLevelEmitterFluid;
import extracells.tile.TileEntityMEBattery;
import extracells.tile.TileEntityMEDropper;
import extracells.tile.TileEntitySolderingStation;
import extracells.tile.TileEntityTerminalFluid;
import extracells.tile.TileEntityTransitionPlaneFluid;
import extracells.tile.TileEntityVoidFluid;
import extracells.tile.TileEntityWalrus;

public class CommonProxy implements IGuiHandler
{
	public void checkForIDMismatches()
	{
		for (BlockEnum entry : BlockEnum.values())
		{
			if (!entry.getBlockClass().isInstance(Block.blocksList[entry.getID()]))
				FMLLog.log(Level.SEVERE, "!IMPORTANT! ExtraCells has found ID mismatches! The Block \"" + entry.getStatName() + "\"with the id " + entry.getID() + " has been overridden by another mod!");
		}

		for (ItemEnum entry : ItemEnum.values())
		{
			if (!entry.getItemClass().isInstance(Item.itemsList[entry.getID() + 256]))
				FMLLog.log(Level.SEVERE, "!IMPORTANT! ExtraCells has found ID mismatches! The Item \"" + entry.getStatName() + "\"with the id " + entry.getID() + " (in config, ingame it'shifted up by 256 by forge) has been overridden by another mod!");
		}
	}

	public void addRecipes()
	{
		try
		{
			ItemStack storagePhysical256k = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 0);
			ItemStack storagePhysical1m = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 1);
			ItemStack storagePhysical4m = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 2);
			ItemStack storagePhysical16m = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 3);

			ItemStack storageFluid1k = new ItemStack(ItemEnum.STORAGEFLUID.getItemInstance(), 1, 0);
			ItemStack storageFluid4k = new ItemStack(ItemEnum.STORAGEFLUID.getItemInstance(), 1, 1);
			ItemStack storageFluid16k = new ItemStack(ItemEnum.STORAGEFLUID.getItemInstance(), 1, 2);
			ItemStack storageFluid64k = new ItemStack(ItemEnum.STORAGEFLUID.getItemInstance(), 1, 3);

			ItemStack containerCell = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 4);

			ItemStack encryptableCell = new ItemStack(ItemEnum.STORAGEPHYSICALDECRYPTED.getItemInstance(), 1, 0);

			ItemStack customCell = new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 5);

			ItemStack physicalKilo = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 0);
			ItemStack physicalMega = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 1);
			ItemStack physicalGiga = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 2);
			ItemStack physicalTera = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 3);
			ItemStack fluidCell = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 4);
			ItemStack fluidSegment = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 5);
			ItemStack fluidBlock = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 6);
			ItemStack fluidCluster = new ItemStack(ItemEnum.STORAGECOMPONENT.getItemInstance(), 1, 7);

			ItemStack advancedStorageCasingPhysical = new ItemStack(ItemEnum.STORAGECASING.getItemInstance(), 1, 0);
			ItemStack advancedStorageCasingFluid = new ItemStack(ItemEnum.STORAGECASING.getItemInstance(), 1, 1);

			ItemStack meItemDropper = new ItemStack(BlockEnum.MEDROPPER.getBlockInstance(), 1);
			ItemStack solderingStation = new ItemStack(BlockEnum.SOLDERINGSTATION.getBlockInstance(), 1);
			ItemStack meBattery = new ItemStack(BlockEnum.MEBATTERY.getBlockInstance(), 1);
			ItemStack hardMEDrive = new ItemStack(BlockEnum.BLASTRESISTANTMEDRIVE.getBlockInstance(), 1);
			ItemStack fluidImportBus = new ItemStack(BlockEnum.FLUIDIMPORT.getBlockInstance(), 1);
			ItemStack fluidExportBus = new ItemStack(BlockEnum.FLUIDEXPORT.getBlockInstance(), 1);
			ItemStack fluidStorageBus = new ItemStack(BlockEnum.FLUIDSTORAGE.getBlockInstance(), 1);
			ItemStack fluidTerminal = new ItemStack(BlockEnum.FLUIDTERMINAL.getBlockInstance(), 1);
			ItemStack transitionPlaneFluid = new ItemStack(BlockEnum.FLUIDTRANSITION.getBlockInstance(), 1);
			ItemStack certusTank = new ItemStack(BlockEnum.CERTUSTANK.getBlockInstance(), 1);
			ItemStack chromia = new ItemStack(BlockEnum.CHROMIA.getBlockInstance(), 1);
			ItemStack levelEmitter = new ItemStack(BlockEnum.FLUIDLEVELEMITTER.getBlockInstance(), 1);
			ItemStack fluidInterface = new ItemStack(BlockEnum.FLUIDINTERFACE.getBlockInstance(), 1);
			ItemStack fluidVoid = new ItemStack(BlockEnum.FLUIDVOID.getBlockInstance(), 1);

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
			{ " P ", "SSS", " P ", 'P', appeng.api.Materials.matProcessorBasic.copy(), 'S', appeng.api.Items.itemCell1k.copy() });

			// Clusters Phsyical
			GameRegistry.addShapedRecipe(physicalKilo, new Object[]
			{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'D', Item.diamond, 'C', appeng.api.Materials.matStorageCluster.copy() });
			GameRegistry.addShapedRecipe(physicalMega, new Object[]
			{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'D', Item.diamond, 'C', physicalKilo });
			GameRegistry.addShapedRecipe(physicalGiga, new Object[]
			{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'D', Item.diamond, 'C', physicalMega });
			GameRegistry.addShapedRecipe(physicalTera, new Object[]
			{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'D', Item.diamond, 'C', physicalGiga });

			// Clusters Fluid
			GameRegistry.addShapedRecipe(fluidCell, new Object[]
			{ "FCF", "CPC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'C', appeng.api.Materials.matFluxCrystal.copy() });
			GameRegistry.addShapedRecipe(fluidSegment, new Object[]
			{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'P', Materials.matProcessorAdvanced.copy(), 'C', fluidCell });
			GameRegistry.addShapedRecipe(fluidBlock, new Object[]
			{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'P', Materials.matProcessorAdvanced.copy(), 'C', fluidSegment });
			GameRegistry.addShapedRecipe(fluidCluster, new Object[]
			{ "FPF", "CGC", "FCF", 'G', Blocks.blkQuartzGlass, 'P', appeng.api.Materials.matProcessorAdvanced.copy(), 'F', appeng.api.Materials.matFluxDust.copy(), 'P', Materials.matProcessorAdvanced.copy(), 'C', fluidBlock });

			// SolderingStation
			GameRegistry.addShapedRecipe(solderingStation, new Object[]
			{ "III", "IDI", "I_I", 'I', Item.ingotIron, 'D', Item.diamond });

			// ME Item Dropper
			GameRegistry.addShapedRecipe(meItemDropper, new Object[]
			{ "CMC", "I_I", "IRI", 'C', Block.cobblestone, 'R', Item.redstone, 'M', appeng.api.Materials.matConversionMatrix.copy(), 'I', Item.ingotIron });

			// ME Backup Battery
			GameRegistry.addShapedRecipe(meBattery, new Object[]
			{ "EFE", "FPF", "EFE", 'E', appeng.api.Blocks.blkEnergyCell, 'F', appeng.api.Materials.matFluxCrystal.copy(), 'P', appeng.api.Materials.matProcessorBasic.copy() });

			// Blast resistant ME Drive
			GameRegistry.addShapedRecipe(hardMEDrive, new Object[]
			{ "OOO", "ODO", "OCO", 'O', Block.obsidian, 'D', appeng.api.Blocks.blkDrive.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy() });

			// ME Fluid Import Bus
			GameRegistry.addShapedRecipe(fluidImportBus, new Object[]
			{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkInputCablePrecision.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', Item.bucketEmpty });

			// ME Fluid Export Bus
			GameRegistry.addShapedRecipe(fluidExportBus, new Object[]
			{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkOutputCablePrecision.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', Item.bucketEmpty });

			// ME Fluid Storage Bus
			GameRegistry.addShapedRecipe(fluidStorageBus, new Object[]
			{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkStorageBus.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', Item.bucketEmpty });

			// ME Fluid Terminal
			GameRegistry.addShapedRecipe(fluidTerminal, new Object[]
			{ "IBI", "ISI", "ICI", 'I', Item.ingotIron, 'S', appeng.api.Blocks.blkTerminal.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', certusTank });

			// ME Fluid Transition Plane
			GameRegistry.addShapedRecipe(transitionPlaneFluid, new Object[]
			{ "BBB", "ITI", "ICI", 'I', Item.ingotIron, 'T', appeng.api.Blocks.blkTransitionPlane.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', Item.bucketEmpty });

			// ME Fluid Transition Plane
			GameRegistry.addShapedRecipe(fluidInterface, new Object[]
			{ "BBB", "ITI", "ICI", 'I', Item.ingotIron, 'T', appeng.api.Blocks.blkInterface.copy(), 'C', appeng.api.Blocks.blkColorlessCableCovered.copy(), 'B', Item.bucketEmpty });

			// Certus Tank
			GameRegistry.addShapedRecipe(certusTank, new Object[]
			{ "GGG", "G_G", "GCG", 'G', Blocks.blkQuartzGlass, 'C', Blocks.blkColorlessCable.copy(), });

			// Chromia
			GameRegistry.addShapedRecipe(chromia, new Object[]
			{ "FFF", "F_F", "FFF", 'F', Item.fishRaw, });

			// Level Emitter
			GameRegistry.addRecipe(new ShapelessOreRecipe(levelEmitter, new Object[]
			{ Blocks.blkLevelEmitter, "dyeBlue" }));

			// Fluid Void
			GameRegistry.addShapedRecipe(fluidVoid, new Object[]
			{ "FIF", "IEI", "FIF", 'F', Materials.matFluxCrystal.copy(), 'E', new ItemStack(Item.enderPearl, 1), 'I', new ItemStack(Item.ingotIron, 1) });
			try 
		    {
		        Class.forName("appeng.api.me.util.ITileCraftingProvider");
				ItemStack fluidCrafter = new ItemStack(BlockEnum.FLUIDCRAFTER.getBlockInstance(), 1);
			   	// Fluid Crafter
				GameRegistry.addShapedRecipe(fluidCrafter, new Object[]
				{ "III", "MPM", "TCT", 'T', certusTank, 'M', Materials.matConversionMatrix.copy(), 'P', Blocks.blkAssembler.copy(), 'I', new ItemStack(Item.ingotIron, 1), 'C', Blocks.blkColorlessCable.copy(), });
			} 
		    catch (ClassNotFoundException e) 
		    {
		    	//AE13
		    }
		} catch (Throwable e)
		{
			FMLLog.log(Level.SEVERE, "There was an ID conflict in extracells! Shutting down now!");
			System.exit(1);
		}
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
		GameRegistry.registerTileEntity(TileEntityLevelEmitterFluid.class, "tileEntityLevelEmitterFluid");
		GameRegistry.registerTileEntity(TileEntityVoidFluid.class, "tileEntityVoidFluid");
		GameRegistry.registerTileEntity(TileEntityInterfaceFluid.class, "tileEntityInterfaceFluid");
		
		try 
	    {
	        Class.forName("appeng.api.me.util.ITileCraftingProvider");
			GameRegistry.registerTileEntity(TileEntityFluidCrafter.class, "tileEntityFluidCrafter");
	    } 
	    catch (ClassNotFoundException e) 
	    {
	    	//AE13
	    }
	}

	public void RegisterRenderers()
	{
		// Only Clientside
	}

	public void RegisterItems()
	{
		for (ItemEnum current : ItemEnum.values())
		{
			try
			{
				current.setItemInstance(current.getItemClass().getConstructor(int.class).newInstance(current.getID()));
			} catch (Throwable e)
			{
			}
		}
	}

	public void RegisterBlocks()
	{
		for (BlockEnum current : BlockEnum.values())
		{
			try
			{
				if(current!=BlockEnum.FLUIDCRAFTER){
				current.setBlockInstance(current.getBlockClass().getConstructor(int.class).newInstance(current.getID()));
				GameRegistry.registerBlock(current.getBlockInstance(), current.getItemBlockClass(), current.getBlockInstance().getUnlocalizedName());
				}else{
					try 
				    {
				        Class.forName("appeng.api.me.util.ITileCraftingProvider");
				        current.setBlockInstance(current.getBlockClass().getConstructor(int.class).newInstance(current.getID()));
						GameRegistry.registerBlock(current.getBlockInstance(), current.getItemBlockClass(), current.getBlockInstance().getUnlocalizedName());
				    } 
				    catch (ClassNotFoundException e) 
				    {
				    	//AE13
				    }
				}
			} catch (Throwable e)
			{
			}
		}
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
				return new GuiTerminalFluid(world, (TileEntityTerminalFluid) tileEntity, player);
			case 2: // GUI Storage Bus Fluid
				return new GuiBusFluidStorage(world, player.inventory, (TileEntityBusFluidStorage) tileEntity);
			case 3: // GUI Import Bus Fluid
				return new GuiBusFluidImport(world, player.inventory, (TileEntityBusFluidImport) tileEntity, player);
			case 4: // GUI Export Bus Fluid
				return new GuiBusFluidExport(world, player.inventory, (TileEntityBusFluidExport) tileEntity, player);
			case 5: // GUI ME Battery
				return new GuiMEBattery(world, (TileEntityMEBattery) tileEntity, player);
			case 6: // GUI ME Fluid Level Emitter
				return new GuiLevelEmitterFluid(player.inventory, (TileEntityLevelEmitterFluid) tileEntity);
			case 7: // GUI ME Fluid Interface
				return new GuiInterfaceFluid(player.inventory, (TileEntityInterfaceFluid) tileEntity);
			case 8: // GUI ME Fluid Crafter
				return new GuiFluidCrafter(player.inventory, ((TileEntityFluidCrafter) tileEntity).getInventory());
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
				return new ContainerTerminalFluid(player, ((TileEntityTerminalFluid) tileEntity).getInventory());
			case 2: // GUI Storage Bus Fluid
				return new ContainerBusFluidStorage(player.inventory, ((TileEntityBusFluidStorage) tileEntity).getInventory());
			case 3: // GUI Import Bus Fluid
				return new ContainerBusFluidImport(player.inventory, ((TileEntityBusFluidImport) tileEntity).getInventory());
			case 4: // GUI Export Bus Fluid
				return new ContainerBusFluidExport(player.inventory, ((TileEntityBusFluidExport) tileEntity).getInventory());
			case 5: // GUI ME Battery
				return new ContainerMEBattery();
			case 6: // GUI ME Fluid Level Emitter
				return new ContainerLevelEmitterFluid(player.inventory, ((TileEntityLevelEmitterFluid) tileEntity).getInventory());
			case 7: // GUI ME Fluid Interface
				return new ContainerInterfaceFluid(player.inventory, ((TileEntityInterfaceFluid) tileEntity).getInventory());
			case 8: // GUI ME Fluid Crafter
				return new ContainerFluidCrafter(player.inventory, ((TileEntityFluidCrafter) tileEntity).getInventory());
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