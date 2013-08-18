package extracells;

import appeng.api.Util;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.blocks.BlockMEDropper;
import extracells.handler.FluidCellHandler;
import extracells.localization.LocalizationHandler;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;

@Mod(modid = "extracells", name = "Extra Cells", version = "1.3.3", dependencies = "required-after:AppliedEnergistics")
@NetworkMod(channels =
{ PacketHandler.channel }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Extracells
{

	@Instance("extracells")
	public static Extracells instance;
	public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(StoragePhysical, 1, 4);
		}
	};

	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static Item Cluster;
	public static Item StoragePhysical;
	public static Item StoragePhysicalEncrypted;
	public static Item StoragePhysicalDecrypted;
	public static Item StorageFluid;
	public static Item Casing;
	public static Item FluidDisplay;
	public static Block SolderingStation;
	public static Block MEDropper;
	public static Block MEBattery;
	public static Block HardMEDrive;
	public static Block BusFluidImport;
	public static Block BusFluidExport;
	public static Block BusFluidStorage;
	public static Block TerminalFluid;
	public static int StoragePhysical_ID;
	public static int StoragePhysicalEncrypted_ID;
	public static int StoragePhysicalDecrypted_ID;
	public static int StorageFluid_ID;
	public static int Cluster_ID;
	public static int Casing_ID;
	public static int FluidDisplay_ID;
	public static int SolderingStation_ID;
	public static int MEDropper_ID;
	public static int MEBattery_ID;
	public static int HardMEDrive_ID;
	public static int BusFluidImport_ID;
	public static int BusFluidExport_ID;
	public static int BusFluidStorage_ID;
	public static int TerminalFluid_ID;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		instance = this;
		LocalizationHandler.loadLanguages();

		// Config
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();
		int clusterTemp = config.getItem("Cluster_ID", 4141, "ID for the storage clusters (crafting)").getInt();
		int storagePhysicalTemp = config.getItem("Cell_ID", 4140, "ID for the storage cells").getInt();
		int storagePhysicalEncryptedTemp = config.getItem("Cell_Encrypted_ID", 4142, "ID for the encrypted storage cell").getInt();
		int storagePhysicalDecryptedTemp = config.getItem("Cell_Decrypted_ID", 4143, "ID for the decrypted storage cell").getInt();
		int casingTemp = config.getItem("Advanced_Storage_Casing ID", 4144, "ID for the advanced storage casing").getInt();
		int fluidDisplayTemp = config.getItem("Fluid_Display_Item_ID", 4145, "ID item used for displaying fluids in the terminal").getInt();
		int storageFluidTemp = config.getItem("StorageFluid_ID", 4146, "ID for the ME Fluid Storages").getInt();
		int solderingStationTemp = config.getBlock("SolderingStation_ID", 500, "ID for the soldering station").getInt();
		int meDropperTemp = config.getBlock("MEDropper_ID", 501, "ID for the ME Item Dropper").getInt();
		int meBatteryTemp = config.getBlock("MEBattery_ID", 502, "ID for the ME Backup Battery").getInt();
		int hardMEDriveTemp = config.getBlock("HardMEDrive_ID", 503, "ID for the Blast Resistant ME Drive").getInt();
		int busFluidImportTemp = config.getBlock("BusFluidImport_ID", 504, "ID for the Fluid Import Bus").getInt();
		int busFluidExportTemp = config.getBlock("BusFluidExport_ID", 505, "ID for the Fluid Export Bus").getInt();
		int busFluidStorageTemp = config.getBlock("BusFluidStorage_ID", 506, "ID for the Fluid Storage Bus").getInt();
		int monitorFluidTemp = config.getBlock("MonitorFluid_ID", 507, "ID for the Fluid Storage Monitor").getInt();
		config.save();

		Cluster_ID = clusterTemp;
		StoragePhysical_ID = storagePhysicalTemp;
		StoragePhysicalEncrypted_ID = storagePhysicalEncryptedTemp;
		StoragePhysicalDecrypted_ID = storagePhysicalDecryptedTemp;
		StorageFluid_ID = storageFluidTemp;
		Casing_ID = casingTemp;
		FluidDisplay_ID = fluidDisplayTemp;
		SolderingStation_ID = solderingStationTemp;
		MEDropper_ID = meDropperTemp;
		MEBattery_ID = meBatteryTemp;
		HardMEDrive_ID = hardMEDriveTemp;
		BusFluidImport_ID = busFluidImportTemp;
		BusFluidExport_ID = busFluidExportTemp;
		BusFluidStorage_ID = busFluidStorageTemp;
		TerminalFluid_ID = monitorFluidTemp;
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.RegisterItems();
		proxy.RegisterBlocks();
		proxy.RegisterRenderers();
		proxy.RegisterTileEntities();
		proxy.addRecipes();
		//Util.addBasicBlackList(extracells.Extracells.FluidDisplay.itemID, OreDictionary.WILDCARD_VALUE);
		Util.getCellRegistry().addCellHandler(new FluidCellHandler());
		LanguageRegistry.instance().addStringLocalization("itemGroup.Extra_Cells", "en_US", "Extra Cells");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{

	}
}