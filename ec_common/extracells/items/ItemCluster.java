package extracells.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import extracells.*;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;

public class ItemCluster extends Item {

	// Item Names
	public static final String[] localized_names = new String[] {
			"Kilo Storage Cluster", "Mega Storage Cluster", "Giga Storage Cluster", "Tera Storage Cluster" };
	public static final String[] meta_names = new String[] { "itemKiloCluster",	"itemMegaCluster", "itemGigaCluster", "itemTeraCluster" };
	// Icons
	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemCluster(int id) {
		super(id);
		this.setMaxStackSize(64);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1) {
		int j = MathHelper.clamp_int(par1, 0, 3);
		return this.icons[j];
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		this.icons = new Icon[meta_names.length];

		for (int i = 0; i < meta_names.length; ++i) {
			this.icons[i] = par1IconRegister.registerIcon("extracells:"
					+ meta_names[i]);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		int i = MathHelper.clamp_int(par1ItemStack.getItemDamage(), 0, 3);
		return super.getUnlocalizedName() + "." + meta_names[i];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
    @SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs,
			List par3List) {
		for (int j = 0; j < 4; ++j) {
			par3List.add(new ItemStack(par1, 1, j));
		}
	}
}
