package extracells.item;

import appeng.api.implementations.items.IStorageComponent;
import extracells.integration.Integration;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageComponent extends ItemECBase implements IStorageComponent {

	private IIcon[] icons;
	public final String[] suffixes = { "physical.256k", "physical.1024k", "physical.4096k", "physical.16384k", "fluid.1k", "fluid.4k", "fluid.16k", "fluid.64k", "fluid.256k", "fluid.1024k", "fluid.4096k", "gas.1k", "gas.4k", "gas.16k", "gas.64k", "gas.256k", "gas.1024k", "gas.4096k" };
	public final int[] size = new int[] { 262144, 1048576, 4194304, 16777216,
			1024, 4096, 16384, 65536, 262144, 1048576, 4194304 };

	public ItemStorageComponent() {
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getBytes(ItemStack is) {
		return this.size[MathHelper.clamp_int(is.getItemDamage(), 0, this.size.length)];
	}

	@Override
	public IIcon getIconFromDamage(int dmg) {
		int j = MathHelper.clamp_int(dmg, 0, this.suffixes.length);
		return this.icons[j];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (int j = 0; j < this.suffixes.length; ++j) {
			if(!(suffixes[j].contains("gas") && !Integration.Mods.MEKANISMGAS.isEnabled()))
				itemList.add(new ItemStack(item, 1, j));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return String.format("extracells.item.storage.component.%s", this.suffixes[itemStack.getItemDamage()]);
	}

	@Override
	public boolean isStorageComponent(ItemStack is) {
		return is.getItem() == this;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icons = new IIcon[this.suffixes.length];

		for (int i = 0; i < this.suffixes.length; ++i) {
			this.icons[i] = iconRegister.registerIcon(
					String.format("extracells:storage.component.%s", this.suffixes[i]));
		}
	}
}
