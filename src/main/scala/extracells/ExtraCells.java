package extracells;

import com.google.common.base.Preconditions;

import javax.annotation.Nullable;
import java.io.File;

import net.minecraft.util.datafix.FixTypes;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fluids.FluidRegistry;

import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import appeng.api.AEApi;
import appeng.api.features.IRegistryContainer;
import extracells.integration.Integration;
import extracells.item.storage.CellDefinition;
import extracells.network.GuiHandler$;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;
import extracells.util.ECConfigHandler;
import extracells.util.ExtraCellsEventHandler;
import extracells.util.NameHandler;
import extracells.util.datafix.BasicCellDataFixer;
import extracells.util.datafix.PortableCellDataFixer;
import extracells.wireless.AEWirelessTermHandler;

@Mod(modid = Constants.MOD_ID, version = Constants.VERSION, name = "Extra Cells", dependencies = "after:waila;required-after:appliedenergistics2")
public class ExtraCells {
	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy = null;

	@Mod.Instance(Constants.MOD_ID)
	public static ExtraCells instance;

	public static final Integration integration = new Integration();
	public static int bcBurnTimeMultiplicator = 4;
	private File configFolder;

	public ExtraCells() {
		FluidRegistry.enableUniversalBucket();
	}

	@Nullable
	private static PacketHandler packetHandler;

	public static PacketHandler getPacketHandler() {
		Preconditions.checkState(packetHandler != null);
		return packetHandler;
	}

	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		packetHandler = new PacketHandler();
		NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler$.MODULE$);

		// Config
		configFolder = event.getModConfigurationDirectory();
		Configuration config = new Configuration(new File(configFolder, "AppliedEnergistics2" + File.separator + "extracells.cfg"));
		ECConfigHandler configHandler = new ECConfigHandler(config);
		configHandler.reload();
		MinecraftForge.EVENT_BUS.register(configHandler);

		integration.preInit();
		proxy.registerItems();
		proxy.registerBlocks();
		CellDefinition.create();
		proxy.registerModels();

		//Moved to preeinit for JSON recipes
		IRegistryContainer registries = AEApi.instance().registries();
		registries.recipes().addNewSubItemResolver(new NameHandler());
	}

	@Mod.EventHandler
	public void init(FMLInitializationEvent event) {
		IRegistryContainer registries = AEApi.instance().registries();
		registries.wireless().registerWirelessHandler(new AEWirelessTermHandler());
		ExtraCellsEventHandler handler = new ExtraCellsEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		proxy.registerMovables();
		proxy.registerRenderers();
		proxy.registerTileEntities();
		proxy.registerFluidBurnTimes();
		proxy.addRecipes(configFolder);
		proxy.registerPackets();
		//RenderingRegistry.registerBlockHandler(new RenderHandler(RenderingRegistry.getNextAvailableRenderId))
		integration.init();

		ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(Constants.MOD_ID, 4);
		fixes.registerFix(FixTypes.ITEM_INSTANCE, new BasicCellDataFixer());
		fixes.registerFix(FixTypes.ITEM_INSTANCE, new PortableCellDataFixer());
	}

	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		integration.postInit();

	}
}
