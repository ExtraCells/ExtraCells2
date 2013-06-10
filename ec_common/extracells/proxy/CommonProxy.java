package extracells.proxy;

import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.extracells;
import extracells.blocks.BlockSolderingStation;
import extracells.gui.GUISolderingStation;
import extracells.items.ItemCell;
import extracells.items.ItemCluster;
import extracells.tile.TileEntitySolderingStation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import extracells.container.ContainerSolderingStation;

public class CommonProxy implements IGuiHandler {
    public void addRecipes() {
        ItemStack cell256k = new ItemStack(extracells.Cell, 1, 0);
        ItemStack cell1m = new ItemStack(extracells.Cell, 1, 1);
        ItemStack cell4m = new ItemStack(extracells.Cell, 1, 2);
        ItemStack cell16m = new ItemStack(extracells.Cell, 1, 3);

        ItemStack cluster256k = new ItemStack(extracells.Cluster, 1, 0);
        ItemStack cluster1m = new ItemStack(extracells.Cluster, 1, 1);
        ItemStack cluster4m = new ItemStack(extracells.Cluster, 1, 2);
        ItemStack cluster16m = new ItemStack(extracells.Cluster, 1, 3);

        ItemStack containerCell = new ItemStack(extracells.Cell, 1, 4);

        ItemStack solderingStation = new ItemStack(extracells.SolderingStation,
                1);

        ItemStack customCell = new ItemStack(extracells.Cell, 1, 5);

        // Normal Cells
        GameRegistry.addShapedRecipe(cell256k,
                new Object[] { "GFG", "FCF", "DDD", 'G', Block.glass, 'F',
                        appeng.api.Materials.matFluxDust, 'D', Item.diamond,
                        'C', new ItemStack(extracells.Cluster, 1, 0) });
        GameRegistry.addShapedRecipe(cell1m,
                new Object[] { "GFG", "FCF", "DDD", 'G', Block.glass, 'F',
                        appeng.api.Materials.matFluxDust, 'D', Item.diamond,
                        'C', new ItemStack(extracells.Cluster, 1, 1) });
        GameRegistry.addShapedRecipe(cell4m,
                new Object[] { "GFG", "FCF", "DDD", 'G', Block.glass, 'F',
                        appeng.api.Materials.matFluxDust, 'D', Item.diamond,
                        'C', new ItemStack(extracells.Cluster, 1, 2) });
        GameRegistry.addShapedRecipe(cell16m,
                new Object[] { "GFG", "FCF", "DDD", 'G', Block.glass, 'F',
                        appeng.api.Materials.matFluxDust, 'D', Item.diamond,
                        'C', new ItemStack(extracells.Cluster, 1, 3) });

        // Cell Container
        GameRegistry.addShapelessRecipe(new ItemStack(extracells.Cell, 1, 4),
                new Object[] { appeng.api.Items.itemCell1k, Block.chest });

        // Clusters
        GameRegistry.addShapedRecipe(cluster256k, new Object[] { "FPF", "CDC",
                "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F',
                appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C',
                appeng.api.Materials.matStorageCluster });
        GameRegistry.addShapedRecipe(cluster1m, new Object[] { "FPF", "CDC",
                "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F',
                appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C',
                new ItemStack(extracells.Cluster, 1, 0) });
        GameRegistry.addShapedRecipe(cluster4m, new Object[] { "FPF", "CDC",
                "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F',
                appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C',
                new ItemStack(extracells.Cluster, 1, 1) });
        GameRegistry.addShapedRecipe(cluster16m, new Object[] { "FPF", "CDC",
                "FCF", 'P', appeng.api.Materials.matProcessorAdvanced, 'F',
                appeng.api.Materials.matFluxDust, 'D', Item.diamond, 'C',
                new ItemStack(extracells.Cluster, 1, 2) });

        // SolderingStation
        GameRegistry.addShapedRecipe(solderingStation, new Object[] { "III",
                "IDI", "I_I", 'I', Item.ingotIron, 'D', Item.diamond });

        // Customizable Cell
        GameRegistry.addShapedRecipe(customCell, new Object[] { "", "", "",
                'P', appeng.api.Materials.matProcessorAdvanced });
    }

    public void RegisterTileEntities() {
        GameRegistry.registerTileEntity(TileEntitySolderingStation.class,
                "tileEntitySolderingStation");
    }

    public void RegisterRenderers() {

    }

    public void RegisterItems() {
        extracells.Cell = new ItemCell(extracells.Cell_ID)
                .setUnlocalizedName("cell");
        extracells.Cluster = new ItemCluster(extracells.Cluster_ID)
                .setUnlocalizedName("cluster");
    }

    public void RegisterBlocks() {
        extracells.SolderingStation = new BlockSolderingStation(
                extracells.SolderingStation_ID, Material.rock);
        GameRegistry.registerBlock(extracells.SolderingStation,
                extracells.SolderingStation.getUnlocalizedName());
    }

    public void RegisterNames() {
        LanguageRegistry.instance();
        // Items
        LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 0),
                ItemCell.localized_names[0]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 1),
                ItemCell.localized_names[1]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 2),
                ItemCell.localized_names[2]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 3),
                ItemCell.localized_names[3]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cell, 1, 4),
                ItemCell.localized_names[4]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 0),
                ItemCluster.localized_names[0]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 1),
                ItemCluster.localized_names[1]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 2),
                ItemCluster.localized_names[2]);
        LanguageRegistry.instance();
        LanguageRegistry.addName(new ItemStack(extracells.Cluster, 1, 3),
                ItemCluster.localized_names[3]);
        LanguageRegistry.instance().addStringLocalization(
                "itemGroup.Extra_Cells", "en_US", "Extra Cells");

        LanguageRegistry.instance();
        // Blocks
        LanguageRegistry.addName(extracells.SolderingStation,
                "Soldering-Station");
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z) {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);
        if (tileEntity != null) {
            switch (ID) {
                case 0: // GUI Soldering Station
                    return null;
                default:
                    return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world,
            int x, int y, int z) {
        TileEntity tileEntity = world.getBlockTileEntity(x, y, z);

        if (tileEntity != null) {
            switch (ID) {
                case 0: // GUI Soldering Station
                    return new ContainerSolderingStation();
                default:
                    return false;
            }
        } else {
            return false;
        }
    }
}
