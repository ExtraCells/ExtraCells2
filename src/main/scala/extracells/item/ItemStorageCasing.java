package extracells.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.integration.Integration;
import extracells.models.ModelManager;

public class ItemStorageCasing extends ItemECBase {

	public final String[] suffixes = { "physical", "fluid", "gas" };

	public ItemStorageCasing() {
		setMaxDamage(0);
		setHasSubtypes(true);
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
		return "extracells.item.storage.casing."
				+ this.suffixes[itemStack.getItemDamage()];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (int i = 0; i < this.suffixes.length; ++i) {
			manager.registerItemModel(item, i, "storage/casing/" + this.suffixes[i]);
		}
	}
}
