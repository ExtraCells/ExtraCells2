package extracells.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.models.ModelManager;

public class ItemOCUpgrade extends ItemECBase /*implements UpgradeItemAEBase*/ {

	public ItemOCUpgrade() {
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return getUnlocalizedName();
	}

	@Override
	public String getUnlocalizedName() {
		return super.getUnlocalizedName().replace("item.extracells", "extracells.item");
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		int tier = 3 - stack.getItemDamage();
		return super.getItemStackDisplayName(stack) + " (Tier " + tier + ")";
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> subItems) {
		for(int i = 0;i < 3;i++){
			subItems.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for(int i = 0;i < 3;i++) {
			manager.registerItemModel(item, i);
		}
	}
}
