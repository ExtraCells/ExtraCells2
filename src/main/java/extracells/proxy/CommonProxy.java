package extracells.proxy;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IMEChest;
import appeng.api.parts.IPartHost;
import appeng.api.recipes.IRecipeHandler;
import appeng.api.recipes.IRecipeLoader;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.container.ContainerFluidStorage;
import extracells.gui.GuiFluidStorage;
import extracells.part.PartECBase;
import extracells.registries.BlockEnum;
import extracells.registries.ItemEnum;
import extracells.registries.PartEnum;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.io.*;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler
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

	@Override
	public Object getServerGuiElement(int Id, EntityPlayer player, World world, int x, int y, int z)
	{
		int partId = Id >> 7;
		ForgeDirection side = ForgeDirection.getOrientation(Id & 0x7F);
		if (side != ForgeDirection.UNKNOWN)
		{
			PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
			return part.getServerGuiElement(player);
		}
		switch (partId)
		{
		case 0:
			TileEntity meChestTe = world.getTileEntity(x, y, z);
			IMEChest meChest = (IMEChest) meChestTe;
			return new ContainerFluidStorage(meChest.getMonitorable(ForgeDirection.UNKNOWN).getFluidInventory(), player);
		default:
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getClientGuiElement(int Id, EntityPlayer player, World world, int x, int y, int z)
	{
		int partId = Id >> 7;
		ForgeDirection side = ForgeDirection.getOrientation(Id & 0x7F);
		if (side != ForgeDirection.UNKNOWN)
		{
			PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
			return part.getClientGuiElement(player);
		}
		switch (partId)
		{
		case 0:
			return new GuiFluidStorage(player);
		default:
			return null;
		}
	}

	public static int getGuiId(PartECBase part)
	{
		return PartEnum.getPartID(part) << 7 | part.getSide().ordinal();
	}

	public static int getGuiId(int neutralId)
	{
		return neutralId << 7 | ForgeDirection.UNKNOWN.ordinal();
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
