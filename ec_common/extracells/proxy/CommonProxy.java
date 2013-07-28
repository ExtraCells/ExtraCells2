package extracells.proxy;

import appeng.api.Items;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.ecCraftingHandler;
import extracells.extracells;
import extracells.blocks.BlockSolderingStation;
import extracells.blocks.BlockMEDropper;
import extracells.container.ContainerSolderingStation;
import extracells.items.ItemCasing;
import extracells.items.ItemCell;
import extracells.items.ItemCluster;
import extracells.items.ItemSecureCellDecrypted;
import extracells.items.ItemSecureCellEncrypted;
import extracells.tile.TileEntityMEDropper;
import extracells.tile.TileEntitySolderingStation;

public class CommonProxy implements IGuiHandler
{
	public void addRecipes()
	{
		GameRegistry.registerCraftingHandler(new ecCraftingHandler());

		ItemStack cell256k = new ItemStack(extracells.Cell, 1, 0);
		ItemStack cell1m = new ItemStack(extracells.Cell, 1, 1);
		ItemStack cell4m = new ItemStack(extracells.Cell, 1, 2);
		ItemStack cell16m = new ItemStack(extracells.Cell, 1, 3);

		ItemStack cluster256k = new ItemStack(extracells.Cluster, 1, 0);
		ItemStack cluster1m = new ItemStack(extracells.Cluster, 1, 1);
		ItemStack cluster4m = new ItemStack(extracells.Cluster, 1, 2);
		ItemStack cluster16m = new ItemStack(extracells.Cluster, 1, 3);

		ItemStack containerCell = new ItemStack(extracells.Cell, 1, 4);

		ItemStack encryptableCell = new ItemStack(extracells.CellDecrypted, 1, 0);

		ItemStack customCell = new ItemStack(extracells.Cell, 1, 5);

		ItemStack advancedStorageCasing = new ItemStack(extracells.Casing, 1, 0);

		ItemStack solderingStation = new ItemStack(extracells.SolderingStation, 1);

		// Advanced Casing
		GameRegistry.addShapedRecipe(advancedStorageCasing, new Object[]
		{ "GFG", "F_F", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond });

		// Normal Cells
		GameRegistry.addShapedRecipe(cell256k, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 0) });
		GameRegistry.addShapelessRecipe(cell256k, new Object[]
		{ extracells.Casing, cluster256k });
		GameRegistry.addShapedRecipe(cell1m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 1) });
		GameRegistry.addShapelessRecipe(cell1m, new Object[]
		{ extracells.Casing, cluster1m });
		GameRegistry.addShapedRecipe(cell4m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 2) });
		GameRegistry.addShapelessRecipe(cell4m, new Object[]
		{ extracells.Casing, cluster4m });
		GameRegistry.addShapedRecipe(cell16m, new Object[]
		{ "GFG", "FCF", "DDD", 'G', Block.glass, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 3) });
		GameRegistry.addShapelessRecipe(cell16m, new Object[]
		{ extracells.Casing, cluster16m });

		// Cell Container
		GameRegistry.addShapelessRecipe(containerCell, new Object[]
		{ appeng.api.Items.itemCell1k, Block.chest });

		// Encryptable Cell
		GameRegistry.addShapedRecipe(encryptableCell, new Object[]
		{ "_II", "ICI", "III", 'I', Item.ingotIron, 'C', Items.itemCell1k });

		// Clusters
		GameRegistry.addShapedRecipe(cluster256k, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', appeng.api.Materials.matStorageCluster });
		GameRegistry.addShapedRecipe(cluster1m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 0) });
		GameRegistry.addShapedRecipe(cluster4m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 1) });
		GameRegistry.addShapedRecipe(cluster16m, new Object[]
		{ "FPF", "CDC", "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F', appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C', new ItemStack(extracells.Cluster, 1, 2) });

		// SolderingStation
		GameRegistry.addShapedRecipe(solderingStation, new Object[]
		{ "III", "IDI", "I_I", 'I', Item.ingotIron, 'D', Item.diamond });

		// Customizable Cell
		GameRegistry.addShapedRecipe(customCell, new Object[]
		{ " P ", "SSS", " P ", 'P', appeng.api.Materials.matProcessorBasic, 'S', appeng.api.Items.itemCell1k });
	}

	public void RegisterTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntitySolderingStation.class, "tileEntitySolderingStation");
		GameRegistry.registerTileEntity(TileEntityMEDropper.class, "tileEntityMEDispenser");
	}

	public void RegisterRenderers()
	{

	}

	public void RegisterItems()
	{
		extracells.Cluster = new ItemCluster(extracells.Cluster_ID).setUnlocalizedName("cluster");
		extracells.Cell = new ItemCell(extracells.Cell_ID).setUnlocalizedName("cell");
		extracells.CellDecrypted = new ItemSecureCellDecrypted(extracells.CellDecrypted_ID).setUnlocalizedName("itemSecureCellEncrypted");
		extracells.CellEncrypted = new ItemSecureCellEncrypted(extracells.CellEncrypted_ID).setUnlocalizedName("itemSecureCellDecrypted");
		extracells.Casing = new ItemCasing(extracells.Casing_ID).setUnlocalizedName("storageCasingAdvanced");
	}

	public void RegisterBlocks()
	{
		extracells.SolderingStation = new BlockSolderingStation(extracells.SolderingStation_ID);
		GameRegistry.registerBlock(extracells.SolderingStation, "SolderingStation");
		extracells.MEDropper = new BlockMEDropper(extracells.MEDropper_ID);
		GameRegistry.registerBlock(extracells.MEDropper, "meDispenser");
	}

	public void RegisterNames()
	{
		LanguageRegistry.instance();
		// Items
		LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 0), ItemCell.localized_names[0]);
		LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 1), ItemCell.localized_names[1]);
		LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 2), ItemCell.localized_names[2]);
		LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 3), ItemCell.localized_names[3]);
		LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 4), ItemCell.localized_names[4]);
		LanguageRegistry.addName(new ItemStack(extracells.CellDecrypted, 1), "Encryptable Cell - Decrypted");
		LanguageRegistry.addName(new ItemStack(extracells.CellEncrypted, 1), "Encryptable Cell - Encrypted");
		LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 0), ItemCluster.localized_names[0]);
		LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 1), ItemCluster.localized_names[1]);
		LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 2), ItemCluster.localized_names[2]);
		LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 3), ItemCluster.localized_names[3]);
		LanguageRegistry.addName(new ItemStack(extracells.Casing, 1, 0), "Advanced Storage Cell Housing");
		LanguageRegistry.instance().addStringLocalization("itemGroup.Extra_Cells", "en_US", "Extra Cells");

		// Blocks
		LanguageRegistry.addName(extracells.SolderingStation, "Soldering-Station");
		LanguageRegistry.addName(extracells.MEDropper, "ME Item Dropper");
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z)
	{
		TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
		if (tileEntity != null)
		{
			switch (ID)
			{
			case 0: // GUI Soldering Station
				return null;
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
			case 0: // GUI Soldering Station
				return new ContainerSolderingStation();
			default:
				return false;
			}
		} else
		{
			return false;
		}
	}
}
