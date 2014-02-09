package extracells.proxy;

import appeng.api.parts.IPartHost;
import cpw.mods.fml.common.network.IGuiHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import extracells.BlockEnum;
import extracells.ItemEnum;
import extracells.item.ItemPartECBase;
import extracells.part.PartECBase;
import extracells.tileentity.TileEntityCertusTank;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

@SuppressWarnings("unused")
public class CommonProxy implements IGuiHandler
{

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
			GameRegistry.registerItem(current.getItem(), current.getInternalName(), "extracells");
		}
	}

	public void RegisterBlocks()
	{
		for (BlockEnum current : BlockEnum.values())
		{
			GameRegistry.registerBlock(current.getBlock(), current.getItemBlockClass(), current.getInternalName());
		}
	}

	@Override
	public Object getServerGuiElement(int Id, EntityPlayer player, World world, int x, int y, int z)
	{
		if (Id > 0)
		{
			int partId = Id >> 7;
			ForgeDirection side = ForgeDirection.getOrientation(Id & 0x7F);
			PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
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
			PartECBase part = (PartECBase) ((IPartHost) world.getTileEntity(x, y, z)).getPart(side);
			return part.getClientGuiElement(player);
		}
		return null;
	}

	public static int getGuiId(PartECBase part)
	{
		return ItemPartECBase.getPartId(part.getClass()) << 7 | part.getSide().ordinal();
	}
}
