package extracells.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import appeng.api.me.items.IStorageComponent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;

public class ItemStorageComponent extends Item implements IStorageComponent
{

	// Localization suffixes
	public static final String[] suffixes = new String[]
	{ "physical.kilo", "physical.mega", "physical.giga", "physical.tera", "fluid.cell", "fluid.segment", "fluid.block", "fluid.cluster" };

	// Sizes
	public static final int[] size = new int[]
	{ 262144, 1048576, 4194304, 16777216, 1024, 4096, 16384, 65536 };

	// Icons
	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemStorageComponent(int id)
	{
		super(id);
		this.setMaxStackSize(64);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Extracells.ModTab);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		return StatCollector.translateToLocal(getUnlocalizedName(itemstack));
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int j)
	{
		return this.icons[j];
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.icons = new Icon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			this.icons[i] = par1IconRegister.registerIcon("extracells:" + "storagecomponent." + suffixes[i]);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		return "item.storagecomponent." + suffixes[i];
	}

	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemID, CreativeTabs creativeTab, List itemList)
	{
		for (int j = 0; j < suffixes.length; ++j)
		{
			itemList.add(new ItemStack(itemID, 1, j));
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
		return true;
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}
}
