package extracells.proxy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import extracells.container.ITickContainer;
import net.minecraft.block.Block;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.FluidRegistry;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import appeng.api.AEApi;
import appeng.api.IAppEngApi;
import appeng.api.movable.IMovableRegistry;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import extracells.network.PacketHandler;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.tileentity.TileEntityCertusTank;
import extracells.tileentity.TileEntityFluidCrafter;
import extracells.tileentity.TileEntityFluidFiller;
import extracells.tileentity.TileEntityFluidInterface;
import extracells.tileentity.TileEntityHardMeDrive;
import extracells.tileentity.TileEntityVibrationChamberFluid;
import extracells.tileentity.TileEntityWalrus;
import extracells.util.FuelBurnTime;
import extracells.util.recipe.RecipeUniversalTerminal;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

public class CommonProxy {

	public CommonProxy(){
		MinecraftForge.EVENT_BUS.register(this);
	}

	private class ExternalRecipeLoader implements IRecipeLoader {

		@Override
		public BufferedReader getFile(String path) throws Exception {
			return new BufferedReader(new FileReader(new File(path)));
		}
	}

	private class InternalRecipeLoader implements IRecipeLoader {

		@Override
		public BufferedReader getFile(String path) throws Exception {
			InputStream resourceAsStream = getClass().getResourceAsStream("/assets/extracells/aerecipes/" + path);
			InputStreamReader reader = new InputStreamReader(resourceAsStream, "UTF-8");
			return new BufferedReader(reader);
		}
	}

	public void addRecipes(File configFolder) {
		IRecipeHandler recipeHandler = AEApi.instance().registries().recipes().createNewRecipehandler();
		File externalRecipe = new File(configFolder.getPath() + File.separator + "AppliedEnergistics2" + File.separator + "extracells.recipe");
		if (externalRecipe.exists()) {
			recipeHandler.parseRecipes(new ExternalRecipeLoader(), externalRecipe.getPath());
		} else {
			recipeHandler.parseRecipes(new InternalRecipeLoader(), "main.recipe");
		}
		recipeHandler.injectRecipes();
	}

	public void registerBlocks() {
		for (BlockEnum current : BlockEnum.values()) {
			registerBlock(current.getBlock());
			registerItem(current.getItem());
		}
	}

	@SubscribeEvent
	public void onRegister(RegistryEvent.Register<IRecipe> event){
		RecipeUniversalTerminal.setRegistryName("UniversalCraftingTerminalRecipe");
		event.getRegistry().register(RecipeUniversalTerminal.THIS());
	}

	public void registerItems() {
		for (ItemEnum current : ItemEnum.values()) {
			if (current.shouldRegister()) {
				registerItem(current.getItem());
			}
		}
	}

	public void registerBlock(Block block) {
		ForgeRegistries.BLOCKS.register(block);
	}

	public void registerItem(Item item) {
		ForgeRegistries.ITEMS.register(item);
	}

	public void registerMovables() {
		IAppEngApi api = AEApi.instance();
		IMovableRegistry movable = api.registries().movable();
		movable.whiteListTileEntity(TileEntityCertusTank.class);
		movable.whiteListTileEntity(TileEntityWalrus.class);
		movable.whiteListTileEntity(TileEntityFluidCrafter.class);
		movable.whiteListTileEntity(TileEntityFluidInterface.class);
		movable.whiteListTileEntity(TileEntityFluidFiller.class);
		movable.whiteListTileEntity(TileEntityHardMeDrive.class);
		movable.whiteListTileEntity(TileEntityVibrationChamberFluid.class);
	}

	public void registerRenderers() {
		// Only Client Side
	}

	public void registerModels() {
		// Only Client Side
	}

	public void registerTileEntities() {
		GameRegistry.registerTileEntity(TileEntityCertusTank.class, "tileEntityCertusTank");
		GameRegistry.registerTileEntity(TileEntityWalrus.class, "tileEntityWalrus");
		GameRegistry.registerTileEntity(TileEntityFluidCrafter.class, "tileEntityFluidCrafter");
		GameRegistry.registerTileEntity(TileEntityFluidInterface.class, "tileEntityFluidInterface");
		GameRegistry.registerTileEntity(TileEntityFluidFiller.class, "tileEntityFluidFiller");
		GameRegistry.registerTileEntity(TileEntityHardMeDrive.class, "tileEntityHardMEDrive");
		GameRegistry.registerTileEntity(TileEntityVibrationChamberFluid.class, "tileEntityVibrationChamberFluid");
	}

	public void registerFluidBurnTimes() {
		FuelBurnTime.registerFuel(FluidRegistry.LAVA, 800);
	}

	public boolean isClient() {
		return false;
	}

	public boolean isServer() {
		return true;
	}

	public void registerPackets() {
		PacketHandler.registerServerPackets();
	}
}
