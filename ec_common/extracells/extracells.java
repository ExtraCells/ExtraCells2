package extracells;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import extracells.blocks.BlockMEDropper;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;

@Mod(modid = "extracells", name = "Extra Cells", version = "1.2.5", dependencies = "required-after:AppliedEnergistics")
@NetworkMod(channels =
{ PacketHandler.CHANNEL_NAME }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class extracells
{

	@Instance("extracells")
	public static extracells instance;
	public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(Cell, 1, 4);
		}
	};

	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static Item Cluster;
	public static Item Cell;
	public static Item CellEncrypted;
	public static Item CellDecrypted;
	public static Item Casing;
	public static Block SolderingStation;
	public static Block MEDropper;
	public static int Cell_ID;
	public static int CellEncrypted_ID;
	public static int CellDecrypted_ID;
	public static int Cluster_ID;
	public static int Casing_ID;
	public static int SolderingStation_ID;
	public static int MEDropper_ID;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		instance = this;

		// Config
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		int clusterTemp = config.getItem("Cluster_ID", 4141, "ID for the storage clusters (crafting)").getInt();
		int cellTemp = config.getItem("Cell_ID", 4140, "ID for the storage cells").getInt();
		int cellEncryptedTemp = config.getItem("Cell_Encrypted_ID", 4142, "ID for the encrypted storage cell").getInt();
		int cellDecryptedTemp = config.getItem("Cell_Decrypted_ID", 4143, "ID for the decrypted storage cell").getInt();
		int casingTemp = config.getItem("Advanced Storage Casing ID", 4144, "ID for the advanced storage casing").getInt();
		int solderingstationTemp = config.getBlock("SolderingStation_ID", 500, "ID for the soldering station").getInt();
		int medropperTemp = config.getBlock("MEDropper_ID", 501, "ID for the ME Item Dropper").getInt();
		config.save();

		Cluster_ID = clusterTemp;
		Cell_ID = cellTemp;
		CellEncrypted_ID = cellEncryptedTemp;
		CellDecrypted_ID = cellDecryptedTemp;
		Casing_ID = casingTemp;
		SolderingStation_ID = solderingstationTemp;
		MEDropper_ID = medropperTemp;
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.RegisterItems();
		proxy.RegisterBlocks();
		proxy.RegisterNames();
		proxy.RegisterRenderers();
		proxy.RegisterTileEntities();
		proxy.addRecipes();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}
}