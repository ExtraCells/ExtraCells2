package extracells;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import extracells.part.ECBasePart;
import extracells.proxy.CommonProxy;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.Configuration;

@Mod(modid = "extracells", name = "Extra Cells", dependencies = "after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2")
public class Extracells
{
	@Instance("extracells")
	public static Extracells instance;

	public static CreativeTabs ModTab = new CreativeTabs("Extra_Cells")
	{
		public ItemStack getIconItemStack()
		{
			return new ItemStack(ItemEnum.PARTITEM.getItemInstance());
		}
	};

	@SidedProxy(clientSide = "extracells.proxy.ClientProxy", serverSide = "extracells.proxy.CommonProxy")
	public static CommonProxy proxy;

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
		ECBasePart.registerParts();
		LanguageRegistry.instance().addStringLocalization("itemGroup.Extra_Cells", "en_US", "Extra Cells");
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		proxy.checkForIDMismatches();
	}
}
