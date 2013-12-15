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
import extracells.handler.FluidCellHandler;
import extracells.localization.LocalizationHandler;
import extracells.network.AbstractPacket;
import extracells.network.PacketHandler;
import extracells.proxy.CommonProxy;
import extracells.render.RenderHandler;

@Mod(modid = "extracells", name = "Extra Cells", dependencies = "required-after:AppliedEnergistics")
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

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.instance().registerGuiHandler(this, proxy);
		instance = this;
		LocalizationHandler.loadLanguages();

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

		debug = config.get("Dev Options", "showFluidsInMETerminal", false, "Dont't activate if you dont want to debug stuff ;)").getBoolean(false);
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

		// EnderNET Support
		FMLInterModComms.sendMessage("endernet", "WhitelistItemNBT", new ItemStack(ItemEnum.STORAGEFLUID.getItemInstance(), 1));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}