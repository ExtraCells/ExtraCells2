package extracells;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;

@Mod(modid = "extracells", name = "Extra Cells", version = "1.1.1b", dependencies = "required-after:AppliedEnergistics")
@NetworkMod(channels = { PacketHandler.CHANNEL_NAME }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class extracells {

    @Instance("extracells")
    public static extracells instance;
    public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells") {
        public ItemStack getIconItemStack() {
            return new ItemStack(Cell, 1, 4);
        }
    };
    @SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
    public static CommonProxy proxy;

    public static Item Cell;
    public static Item Cluster;
    public static Block SolderingStation;
    public static int Cell_ID;
    public static int Cluster_ID;
    public static int SolderingStation_ID;

    @PreInit
    public void preInit(FMLPreInitializationEvent event) {
        NetworkRegistry.instance().registerGuiHandler(this, proxy);
        instance = this;

        // config
        Configuration config = new Configuration(
                event.getSuggestedConfigurationFile());
        config.load();
        int cellTemp = config.getItem("Cell_ID", 4140,
                "ID for the storage cells").getInt();
        int clusterTemp = config.getItem("Cluster_ID", 4141,
                "ID for the storage clusters (crafting)").getInt();
        int solderingstationTemp = config.getBlock("SolderingStation_ID", 500,
                "ID for the soldering station").getInt();
        config.save();
        SolderingStation_ID = solderingstationTemp;
        Cell_ID = cellTemp;
        Cluster_ID = clusterTemp;
    }

    @Init
    public void init(FMLInitializationEvent event) {
        proxy.RegisterItems();
        proxy.RegisterBlocks();
        proxy.RegisterNames();
        proxy.RegisterRenderers();
        proxy.RegisterTileEntities();
        proxy.addRecipes();
    }
}