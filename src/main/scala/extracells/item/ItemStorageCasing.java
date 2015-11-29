package extracells.item;

import extracells.Extracells;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageCasing extends ItemECBase {

	private IIcon[] icons;
	public final String[] suffixes = { "physical", "fluid", "gas" };

	public ItemStorageCasing() {
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(Extracells.ModTab());
	}

	@Override
	public IIcon getIconFromDamage(int dmg) {
		int j = MathHelper.clamp_int(dmg, 0, this.icons.length - 1);
		return this.icons[j];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (int j = 0; j < this.suffixes.length; ++j) {
			itemList.add(new ItemStack(item, 1, j));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.storage.casing."
				+ this.suffixes[itemStack.getItemDamage()];
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icons = new IIcon[this.suffixes.length];

		for (int i = 0; i < this.suffixes.length; ++i) {
			this.icons[i] = iconRegister.registerIcon("extracells:"
					+ "storage.casing." + this.suffixes[i]);
		}
	}
}
