package extracells.item;

import appeng.api.implementations.items.IStorageComponent;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageComponent extends Item implements IStorageComponent
{
	private IIcon[] icons;
	public final String[] suffixes =
	{ "physical.kilo", "physical.mega", "physical.giga", "physical.tera", "fluid.cell", "fluid.segment", "fluid.block", "fluid.cluster", "fluid.kilo", "fluid.mega", "fluid.giga" };
	public final int[] size = new int[]
	{ 262144, 1048576, 4194304, 16777216, 1024, 4096, 16384, 65536, 262144, 1048576, 4194304 };

	public ItemStorageComponent()
	{
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "extracells.item.storage.component." + suffixes[itemStack.getItemDamage()];
	}

	@Override
	public IIcon getIconFromDamage(int dmg)
	{
		int j = MathHelper.clamp_int(dmg, 0, suffixes.length);
		return icons[j];
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		icons = new IIcon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			icons[i] = iconRegister.registerIcon("extracells:" + "storage.component." + suffixes[i]);
		}
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList)
	{
		for (int j = 0; j < suffixes.length; ++j)
		{
			itemList.add(new ItemStack(item, 1, j));
		}
	}

	@Override
	public int getBytes(ItemStack is)
	{
		return size[is.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent(ItemStack is)
	{
		return is.getItem() == this;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}
}
