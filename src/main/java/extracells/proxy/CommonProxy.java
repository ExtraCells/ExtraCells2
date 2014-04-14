package extracells.proxy;

import appeng.api.AEApi;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.tileentity.TileEntityCertusTank;

import java.io.*;

@SuppressWarnings("unused")
public class CommonProxy
{

	public void registerMovables()
	{
	}

	public void addRecipes(File configFolder)
	{
		IRecipeHandler recipeHandler = AEApi.instance().registries().recipes().createNewRecipehandler();
		File externalRecipe = new File(configFolder.getPath() + File.separator + "AppliedEnergistics2" + File.separator + "extracells.recipe");
		if (externalRecipe.exists())
		{
			recipeHandler.parseRecipes(new ExternalRecipeLoader(), externalRecipe.getPath());
		} else
		{
			recipeHandler.parseRecipes(new InternalRecipeLoader(), "/assets/extracells/recipes/main.recipe");
		}
		recipeHandler.registerHandlers();
	}

	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityCertusTank.class, "tileEntityCertusTank");
	}

	public void registerRenderers()
	{
		// Only Clientside
	}

	public void registerItems()
	{
		for (ItemEnum current : ItemEnum.values())
		{
			GameRegistry.registerItem(current.getItem(), current.getInternalName());
		}
	}

	public void registerBlocks()
	{
		for (BlockEnum current : BlockEnum.values())
		{
			GameRegistry.registerBlock(current.getBlock(), current.getItemBlockClass(), current.getInternalName());
		}
	}

	private class InternalRecipeLoader implements IRecipeLoader
	{

		@Override
		public BufferedReader getFile(String path) throws Exception
		{
			InputStream resourceAsStream = getClass().getResourceAsStream(path);
			InputStreamReader reader = new InputStreamReader(resourceAsStream, "UTF-8");
			return new BufferedReader(reader);
		}
	}

	private class ExternalRecipeLoader implements IRecipeLoader
	{

		@Override
		public BufferedReader getFile(String path) throws Exception
		{
			return new BufferedReader(new FileReader(new File(path)));
		}
	}
}
