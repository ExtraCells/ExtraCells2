package extracells.proxy;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import appeng.api.Items;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.CraftingHandler;
import extracells.Extracells;
import extracells.blocks.BlockBusFluidImport;
import extracells.blocks.BlockBusFluidStorage;
import extracells.blocks.BlockHardMEDrive;
import extracells.blocks.BlockMEBattery;
import extracells.blocks.BlockMEDropper;
import extracells.blocks.BlockSolderingStation;
import extracells.container.ContainerHardMEDrive;
import extracells.container.ContainerSolderingStation;
import extracells.gui.GUIHardMEDrive;
import extracells.items.ItemBlockSpecial;
import extracells.items.ItemCasing;
import extracells.items.ItemCell;
import extracells.items.ItemCluster;
import extracells.items.ItemSecureCellDecrypted;
import extracells.items.ItemSecureCellEncrypted;
import extracells.tile.TileEntityBusFluidImport;
import extracells.tile.TileEntityBusFluidStorage;
import extracells.tile.TileEntityHardMEDrive;
import extracells.tile.TileEntityMEBattery;
import extracells.tile.TileEntityMEDropper;
import extracells.tile.TileEntitySolderingStation;

public class CommonProxy implements IGuiHandler
{
	public void addRecipes()
	{
		GameRegistry.registerCraftingHandler(new CraftingHandler());

		ItemStack cell256k = new ItemStack(Extracells.Cell, 1, 0);
		ItemStack cell1m = new ItemStack(Extracells.Cell, 1, 1);
		ItemStack cell4m = new ItemStack(Extracells.Cell, 1, 2);
		ItemStack cell16m = new ItemStack(Extracells.Cell, 1, 3);

		ItemStack cluster256k = new ItemStack(Extracells.Cluster, 1, 0);
		ItemStack cluster1m = new ItemStack(Extracells.Cluster, 1, 1);
		ItemStack cluster4m = new ItemStack(Extracells.Cluster, 1, 2);
		ItemStack cluster16m = new ItemStack(Extracells.Cluster, 1, 3);

		ItemStack containerCell = new ItemStack(Extracells.Cell, 1, 4);

		ItemStack encryptableCell = new ItemStack(Extracells.CellDecrypted, 1, 0);

		ItemStack customCell = new ItemStack(Extracells.Cell, 1, 5);

		ItemStack advancedStorageCasing = new ItemStack(Extracells.Casing, 1, 0);

		ItemStack meItemDropper = new ItemStack(Extracells.MEDropper, 1);
		ItemStack solderingStation = new ItemStack(Extracells.SolderingStation, 1);
		ItemStack meBattery = new ItemStack(Extracells.MEBattery, 1);
		ItemStack hardMEDrive = new ItemStack(Extracells.HardMEDrive, 1);

		// Advanced Casing
		GameRegistry.addShapedRecipe(advancedStorageCasing, new Object[]
		{ "GFG", "F_F", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond });

		// Normal Cells
		GameRegistry.addShapedRecipe(cell256k, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 0) });
		GameRegistry.addShapelessRecipe(cell256k, new Object[]
		{ Extracells.Casing, cluster256k });
		GameRegistry.addShapedRecipe(cell1m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 1) });
		GameRegistry.addShapelessRecipe(cell1m, new Object[]
		{ Extracells.Casing, cluster1m });
		GameRegistry.addShapedRecipe(cell4m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 2) });
		GameRegistry.addShapelessRecipe(cell4m, new Object[]
		{ Extracells.Casing, cluster4m });
		GameRegistry.addShapedRecipe(cell16m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(Extracells.Cluster, 1, 3) });
		GameRegistry.addShapelessRecipe(cell16m, new Object[]
		{ Extracells.Casing, cluster16m });

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
	}

	public void RegisterTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntitySolderingStation.class, "tileEntitySolderingStation");
		GameRegistry.registerTileEntity(TileEntityMEDropper.class, "tileEntityMEDropper");
		GameRegistry.registerTileEntity(TileEntityMEBattery.class, "tileEntityMEBattery");
		GameRegistry.registerTileEntity(TileEntityHardMEDrive.class, "tileEntityHardMEDrive");
		GameRegistry.registerTileEntity(TileEntityBusFluidImport.class, "tileEntityBusFluidImport");
		GameRegistry.registerTileEntity(TileEntityBusFluidStorage.class, "tileEntityBusFluidStorage");
	}

	public void RegisterRenderers()
	{
		// Only Clientside
	}

	public void RegisterItems()
	{
		Extracells.Cluster = new ItemCluster(Extracells.Cluster_ID);
		Extracells.Cell = new ItemCell(Extracells.Cell_ID);
		Extracells.CellDecrypted = new ItemSecureCellDecrypted(Extracells.CellDecrypted_ID);
		Extracells.CellEncrypted = new ItemSecureCellEncrypted(Extracells.CellEncrypted_ID);
		Extracells.Casing = new ItemCasing(Extracells.Casing_ID);
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
		Extracells.BusFluidStorage = new BlockBusFluidStorage(Extracells.BusFluidStorage_ID);
		GameRegistry.registerBlock(Extracells.BusFluidStorage, ItemBlockSpecial.class, Extracells.BusFluidStorage.getUnlocalizedName());
	}

	public void RegisterNames()
	{
		LanguageRegistry.instance().addStringLocalization("itemGroup.Extra_Cells", "en_US", "Extra Cells");
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
				return new GUIHardMEDrive(player.inventory, (TileEntityHardMEDrive) tileEntity);
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