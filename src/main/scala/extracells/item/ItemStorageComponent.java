package extracells.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.implementations.items.IStorageComponent;
import extracells.integration.Integration;
import extracells.models.ModelManager;

public class ItemStorageComponent extends ItemECBase implements IStorageComponent {

	public final String[] suffixes = { "physical.256k", "physical.1024k", "physical.4096k", "physical.16384k", "fluid.1k", "fluid.4k", "fluid.16k", "fluid.64k", "fluid.256k", "fluid.1024k", "fluid.4096k", "gas.1k", "gas.4k", "gas.16k", "gas.64k", "gas.256k", "gas.1024k", "gas.4096k" };
	public final int[] size = new int[] { 262144, 1048576, 4194304, 16777216,
			1024, 4096, 16384, 65536, 262144, 1048576, 4194304 };

	public ItemStorageComponent() {
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	public int getBytes(ItemStack is) {
		return this.size[is.getItemDamage()];
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		if (itemStack.getItemDamage() >= 4)
			return EnumRarity.RARE;
		return EnumRarity.EPIC;
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
		return "extracells.item.storage.component."
				+ this.suffixes[itemStack.getItemDamage()];
	}

	@Override
	public boolean isStorageComponent(ItemStack is) {
		return is.getItem() == this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (int i = 0; i < this.suffixes.length; ++i) {
			manager.registerItemModel(item, i, "storage/component/" + this.suffixes[i]);
		}
	}
}
