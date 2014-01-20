package extracells;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.Util;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.gui.widget.WidgetFluidModes;
import extracells.handler.FluidCellHandler;
import extracells.integration.logisticspipes.LPHelper;
import extracells.network.AbstractPacket;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;
import extracells.render.RenderHandler;

@Mod(modid = "extracells", name = "Extra Cells", dependencies = "after:LogisticsPipes|Main;after:Waila;required-after:AppliedEnergistics")
@NetworkMod(channels =
{ AbstractPacket.CHANNEL }, clientSideRequired = true, serverSideRequired = false, packetHandler = PacketHandler.class)
public class Extracells
{
	@Instance("extracells")
	public static Extracells instance;
	public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(ItemEnum.STORAGEPHYSICAL.getItemInstance(), 1, 4);
		}
	};

	public static int renderID;

	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static boolean debug;
	public static boolean shortenedBuckets;

	public static int tickRateExport;
	public static int tickRateImport;
	public static int tickRateStorage;
	
	public static LPHelper lpHelper;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		instance = this;

		// Config
		Configuration config = new Configuration(event.getSuggestedConfigurationFile());
		config.load();

		// Items
		for (ItemEnum current : ItemEnum.values())
		{
			current.setID(config.getItem(current.getIDName() + "_ID", current.getID(), current.getDescription()).getInt());
		}

		// Blocks
		for (BlockEnum current : BlockEnum.values())
		{
			current.setID(config.getBlock(current.getIDName() + "_ID", current.getID(), current.getDescription()).getInt());
		}

		// Tick Rates
		tickRateExport = config.get("Tick Rates", "tickRateExportBus", 20, "Every x ticks the export bus ticks. mb/t get adjusted automatically ;)").getInt();
		tickRateImport = config.get("Tick Rates", "tickRateImportBus", 20, "Every x ticks the import bus ticks. mb/t get adjusted automatically ;)").getInt();
		tickRateStorage = config.get("Tick Rates", "tickRateStorageBus", 20, "Every x ticks the storage bus ticks. mb/t get adjusted automatically ;)").getInt();

		// Fluid Mode Settings
		WidgetFluidModes.FluidMode.DROPS.setAmount(config.get("Fluid Rates", "rateDrop", 20, "The Amount of Fluid being filled/drained per tick on the \"Drop\"-Amount").getInt());
		WidgetFluidModes.FluidMode.QUART.setAmount(config.get("Fluid Rates", "rateQuart", 250, "The Amount of Fluid being filled/drained per tick on the \"Quart\"-Amount").getInt());
		WidgetFluidModes.FluidMode.BUCKETS.setAmount(config.get("Fluid Rates", "rateBucket", 1000, "The Amount of Fluid being filled/drained per tick on the \"Bucket\"-Amount").getInt());
		WidgetFluidModes.FluidMode.DROPS.setCost(config.get("Energy Rates", "rateDrop", 5.0D, "The Energy Cost per fill/drain operation on the \"Drop\"-Amount").getDouble(5.0D));
		WidgetFluidModes.FluidMode.QUART.setCost(config.get("Energy Rates", "rateQuart", 30.0D, "The Energy Cost per fill/drain operation on the \"Quart\"-Amount").getDouble(30.0D));
		WidgetFluidModes.FluidMode.BUCKETS.setCost(config.get("Energy Rates", "rateBucket", 60.0D, "The Energy Cost per fill/drain operation on the \"Bucket\"-Amount").getDouble(60.0D));

		if (tickRateExport <= 0)
			tickRateExport = 20;
		if (tickRateImport <= 0)
			tickRateImport = 20;
		if (tickRateStorage <= 0)
			tickRateStorage = 20;

		debug = config.get("Dev Options", "showFluidsInMETerminal", false, "Dont't activate if you dont want to debug stuff ;)").getBoolean(false);
		shortenedBuckets = config.get("Render Options", "shortenBucketsInTerminal", true, "Do you want to show 1kB or 1000000mB?").getBoolean(true);
		config.save();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.RegisterItems();
		proxy.RegisterBlocks();
		proxy.RegisterRenderers();
		proxy.RegisterTileEntities();
		proxy.addRecipes();
		if (!debug)
			Util.addBasicBlackList(ItemEnum.FLUIDDISPLAY.getItemInstance().itemID, OreDictionary.WILDCARD_VALUE);
		Util.getCellRegistry().addCellHandler(new FluidCellHandler());
		LanguageRegistry.instance().addStringLocalization("itemGroup.Extra_Cells", "en_US", "Extra Cells");
		renderID = RenderingRegistry.getNextAvailableRenderId();
		RenderHandler handler = new RenderHandler(renderID);
		RenderingRegistry.registerBlockHandler(handler);

		// WAILA Support
		FMLInterModComms.sendMessage("Waila", "register", "extracells.integration.WAILA.WailaDataProvider.callbackRegister");

		
		// AE Spatial Storage Support
		proxy.registerMovables();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.checkForIDMismatches();
		//LP Support
		lpHelper = new LPHelper();
	}
}
