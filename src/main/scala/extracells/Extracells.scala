package extracells

import appeng.api.AEApi
import cpw.mods.fml.client.registry.RenderingRegistry
import cpw.mods.fml.common.Mod.EventHandler
import cpw.mods.fml.common.event.{
  FMLInitializationEvent,
  FMLModIdMappingEvent,
  FMLPostInitializationEvent,
  FMLPreInitializationEvent
}
import cpw.mods.fml.common.network.NetworkRegistry
import cpw.mods.fml.common.{FMLCommonHandler, Loader, Mod, SidedProxy}
import extracells.integration.Integration
import extracells.integration.ae2fc.FluidCraft
import extracells.network.{ChannelHandler, GuiHandler}
import extracells.proxy.CommonProxy
import extracells.registries.ItemEnum
import extracells.render.RenderHandler
import extracells.util.{
  AdvancedCellHandler,
  ExtraCellsEventHandler,
  FluidCellHandler,
  NameHandler
}
import extracells.util.AdvancedCellHandler
import extracells.util.VoidCellHandler
import extracells.wireless.AEWirelessTermHandler
import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.config.Configuration

import java.io.File

@Mod(
  modid = "extracells",
  name = "Extra Cells",
  version = "GRADLETOKEN_VERSION",
  modLanguage = "scala",
  dependencies =
    "after:LogisticsPipes|Main;after:Waila;required-after:appliedenergistics2"
)
object Extracells {

  @SidedProxy(
    clientSide = "extracells.proxy.ClientProxy",
    serverSide = "extracells.proxy.CommonProxy"
  )
  var proxy: CommonProxy = null

  var VERSION = ""
  var basePartSpeed = 125
  var bcBurnTimeMultiplicator = 4
  var terminalUpdateInterval = 20
  var configFolder: File = null
  var shortenedBuckets = true
  var dynamicTypes = true
  val integration = new Integration

  val ModTab = new CreativeTabs("Extra_Cells") {
    override def getIconItemStack = new ItemStack(ItemEnum.FLUIDSTORAGE.getItem)
    override def getTabIconItem = ItemEnum.FLUIDSTORAGE.getItem
  }

  @EventHandler
  def init(event: FMLInitializationEvent) {
    AEApi.instance.registries.recipes.addNewSubItemResolver(new NameHandler)
    AEApi.instance.registries.wireless
      .registerWirelessHandler(new AEWirelessTermHandler)
    AEApi.instance.registries.cell.addCellHandler(new FluidCellHandler)
    AEApi.instance.registries.cell.addCellHandler(new AdvancedCellHandler)
    AEApi.instance.registries.cell.addCellHandler(new VoidCellHandler)
    val handler = new ExtraCellsEventHandler
    FMLCommonHandler.instance.bus.register(handler)
    MinecraftForge.EVENT_BUS.register(handler)
    proxy.registerMovables()
    proxy.registerRenderers()
    proxy.registerTileEntities()
    proxy.registerFluidBurnTimes()
    proxy.addRecipes(configFolder)
    ChannelHandler.registerMessages()
    RenderingRegistry.registerBlockHandler(
      new RenderHandler(RenderingRegistry.getNextAvailableRenderId)
    )
    integration.init()
  }

  @EventHandler
  def postInit(event: FMLPostInitializationEvent) {
    integration.postInit()
  }

  @EventHandler
  def preInit(event: FMLPreInitializationEvent) {
    VERSION = Loader.instance.activeModContainer.getVersion
    configFolder = event.getModConfigurationDirectory

    NetworkRegistry.INSTANCE.registerGuiHandler(this, GuiHandler)

    // Config
    val config = new Configuration(
      new File(
        configFolder.getPath + File.separator + "AppliedEnergistics2" + File.separator + "extracells.cfg"
      )
    )
    config.load()
    shortenedBuckets = config
      .get(
        "Tooltips",
        "shortenedBuckets",
        true,
        "Shall the guis shorten large mB values?"
      )
      .getBoolean(true)
    dynamicTypes = config
      .get(
        "Storage Cells",
        "dynamicTypes",
        true,
        "Should the mount of bytes needed for a new type depend on the cellsize?"
      )
      .getBoolean(true)
    terminalUpdateInterval = config
      .get(
        "Terminal",
        "Interval",
        20,
        "How often is the fluid in the terminal updated?"
      )
      .getInt(20)
    basePartSpeed = config
      .get(
        "BaseFluidPart",
        "Interval",
        125,
        "Base fluid part import export speed "
      )
      .getInt(125)

    integration.loadConfig(config)

    config.save()

    proxy.registerItems()
    proxy.registerBlocks()
    integration.preInit()
  }

  @EventHandler
  def handleRemap(event: FMLModIdMappingEvent): Unit = {
    if (Integration.Mods.FLUIDCRAFT.isEnabled) {
      FluidCraft.onRemapEvent(event)
    }
  }
}
