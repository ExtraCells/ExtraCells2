package extracells;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import extracells.network.ChannelHandler;
import extracells.part.PartECBase;
import extracells.proxy.CommonProxy;
import extracells.render.RenderHandler;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;

import java.io.File;

@Mod(modid = "extracells", name = "Extra Cells", dependencies = "after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2")
public class Extracells
{
	@Instance("extracells")
	public static Extracells instance;

	public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(ItemEnum.FLUIDSTORAGE.getItem());
		}

		@Override
		public Item getTabIconItem()
		{
			return ItemEnum.FLUIDSTORAGE.getItem();
		}
	};

	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static int renderID;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
		instance = this;

		// Config
		Configuration config = new Configuration(new File(event.getModConfigurationDirectory().getPath() + File.separator + "AppliedEnergistics2" + File.separator + "extracells.cfg"));
		config.load();
		// DO something
		config.save();

		proxy.RegisterItems();
		proxy.RegisterBlocks();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		proxy.RegisterRenderers();
		proxy.registerTileEntities();
		proxy.addRecipes();
		ChannelHandler.setChannels(NetworkRegistry.INSTANCE.newChannel("ExtraCells", new ChannelHandler()));
		PartECBase.registerParts();
		RenderingRegistry.registerBlockHandler(new RenderHandler(renderID = RenderingRegistry.getNextAvailableRenderId()));
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
	}
}
