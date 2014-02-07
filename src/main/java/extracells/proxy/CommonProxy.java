package extracells.proxy;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.BlockEnum;
import extracells.ItemEnum;
import extracells.item.ItemPartECBase;
import extracells.part.PartECBase;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

import java.util.logging.Level;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler
{
	public void checkForIDMismatches()
	{
		for (BlockEnum entry : BlockEnum.values())
		{
			if (!entry.getBlockClass().isInstance(Block.blocksList[entry.getID()]))
				FMLLog.log(Level.SEVERE, "!IMPORTANT! ExtraCells has found ID mismatches! The Block \"" + entry.getStatName() + "\"with the id " + entry.getID() + " has been overridden by another mod!");
		}

		for (ItemEnum entry : ItemEnum.values())
		{
			if (!entry.getItemClass().isInstance(Item.itemsList[entry.getID() + 256]))
				FMLLog.log(Level.SEVERE, "!IMPORTANT! ExtraCells has found ID mismatches! The Item \"" + entry.getStatName() + "\"with the id " + entry.getID() + " (in config, ingame it'shifted up by 256 by forge) has been overridden by another mod!");
		}
	}

	public void registerMovables()
	{
	}

	public void addRecipes()
	{
	}

	public void registerTileEntities()
	{
		GameRegistry.registerTileEntity(TileEntityCertusTank.class, "tileEntityCertusTank");
	}

	public void RegisterRenderers()
	{
		// Only Clientside
	}

	public void RegisterItems()
	{
		for (ItemEnum current : ItemEnum.values())
		{
			try
			{
				current.setItemInstance(current.getItemClass().getConstructor(int.class).newInstance(current.getID()));
				GameRegistry.registerItem(current.getItemInstance(), current.getItemInstance().getUnlocalizedName(), "extracells");
			} catch (Throwable e)
			{
			}
		}
	}

	public void RegisterBlocks()
	{
		for (BlockEnum current : BlockEnum.values())
		{
			try
			{
				current.setBlockInstance(current.getBlockClass().getConstructor(int.class).newInstance(current.getID()));
				GameRegistry.registerBlock(current.getBlockInstance(), current.getItemBlockClass(), current.getBlockInstance().getUnlocalizedName());

			} catch (Throwable e)
			{
			}
		}
	}

	@Override
	public Object getServerGuiElement(int Id, EntityPlayer player, World world, int x, int y, int z)
	{
		if (Id > 0)
		{
			int partId = Id >> 7;
			ForgeDirection side = ForgeDirection.getOrientation(Id & 0x7F);
			PartECBase part = (PartECBase) ((IPartHost) world.getBlockTileEntity(x, y, z)).getPart(side);
			return part.getServerGuiElement(player);
		}
		return null;
	}

	@Override
	public Object getClientGuiElement(int Id, EntityPlayer player, World world, int x, int y, int z)
	{
		if (Id > 0)
		{
			int partId = Id >> 7;
			ForgeDirection side = ForgeDirection.getOrientation(Id & 0x7F);
			PartECBase part = (PartECBase) ((IPartHost) world.getBlockTileEntity(x, y, z)).getPart(side);
			return part.getClientGuiElement(player);
		}
		return null;
	}

	public static int getGuiId(PartECBase part)
	{
		return ItemPartECBase.getPartId(part.getClass()) << 7 | part.getSide().ordinal();
	}
}
