package extracells.item;

import appeng.api.AEApi;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class ItemECBasePart extends Item implements IPartItem
{
	private static List<IPart> ecParts = new ArrayList<IPart>();

	public ItemECBasePart(int itemId)
	{
		super(itemId);
		setCreativeTab(Extracells.ModTab);
		AEApi.instance().partHelper().setItemBusRenderer(this);
	}

	@Override
	public IPart createPartFromItemStack(ItemStack is)
	{
		return ecParts.get(Integer.valueOf(is.getItemDamage()));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	public void getSubItems(int itemID, CreativeTabs creativeTab, List itemList)
	{
		for (int i = 0; i < ecParts.size(); i++)
		{
			if (ecParts.get(i) != null)
				itemList.add(new ItemStack(itemID, 1, i));
		}
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return AEApi.instance().partHelper().placeBus(is, x, y, z, side, player, world);
	}

	public static int registerPart(IPart part)
	{
		ecParts.add(part);
		return ecParts.indexOf(part);
	}
}
