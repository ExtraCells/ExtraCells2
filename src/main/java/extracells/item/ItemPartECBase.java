package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.registries.PartEnum;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;

public class ItemPartECBase extends Item implements IPartItem, IItemGroup
{
	public ItemPartECBase()
	{
		AEApi.instance().partHelper().setItemBusRenderer(this);

		for (PartEnum part : PartEnum.values())
		{
			Map<Upgrades, Integer> possibleUpgradesList = part.getUpgrades();
			for (Upgrades upgrade : possibleUpgradesList.keySet())
			{
				upgrade.registerItem(new ItemStack(this, 1, part.ordinal()), possibleUpgradesList.get(upgrade));
			}
		}
	}

	@Override
	public void registerIcons(IIconRegister _iconRegister)
	{
	}

	@Override
	public IPart createPartFromItemStack(ItemStack itemStack)
	{
		try
		{
			return PartEnum.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, PartEnum.values().length - 1)].newInstance(itemStack);
		} catch (Throwable e)
		{
			System.out.println("SHOULD NOT HAPPEN!");
			e.printStackTrace();
			return null;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList)
	{
		for (int i = 0; i < PartEnum.values().length; i++)
		{
			itemList.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return AEApi.instance().partHelper().placeBus(is, x, y, z, side, player, world);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return PartEnum.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, PartEnum.values().length - 1)].getUnlocalizedName();
	}

	@Override
	public String getUnlocalizedGroupName(ItemStack itemStack)
	{
		return PartEnum.values()[MathHelper.clamp_int(itemStack.getItemDamage(), 0, PartEnum.values().length - 1)].getGroupName();
	}
}
