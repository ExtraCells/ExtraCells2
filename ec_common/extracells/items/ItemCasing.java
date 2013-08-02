package extracells.items;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;

public class ItemCasing extends Item
{
	// Icons
	@SideOnly(Side.CLIENT)
	private Icon icon;

	public ItemCasing(int id)
	{
		super(id);
		this.setCreativeTab(Extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1)
	{
		return icon;
	}

	@Override
	public void registerIcons(IconRegister IconRegister)
	{
		this.icon = IconRegister.registerIcon("extracells:casing.advanced");
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		return this.getLocalizedName(itemstack);
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		return "item.casing.advanced";
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.uncommon;
	}
}
