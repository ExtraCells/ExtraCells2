package extracells.item;

import appeng.api.implementations.ICraftingPatternItem;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import extracells.crafting.CraftingPattern;
import extracells.crafting.CraftingPattern2;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemInternalCraftingPattern extends ItemECBase implements ICraftingPatternItem {

	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack is, World w) {
		if (is == null || w == null)
			return null;
		switch (is.getItemDamage()) {
		case 0:
			if (is.hasTagCompound() && is.getTagCompound().hasKey("item")) {
				ItemStack s = ItemStack.loadItemStackFromNBT(is.getTagCompound().getCompoundTag("item"));
				if (s != null && s.getItem() instanceof ICraftingPatternItem)
					return new CraftingPattern(((ICraftingPatternItem) s.getItem()).getPatternForItem(s, w));
			}
			return null;
		case 1:
			if (is.hasTagCompound() && is.getTagCompound().hasKey("item")) {
				ItemStack s = ItemStack.loadItemStackFromNBT(is.getTagCompound().getCompoundTag("item"));
				if (s != null && s.getItem() instanceof ICraftingPatternItem)
					return new CraftingPattern2(((ICraftingPatternItem) s.getItem()).getPatternForItem(s, w));
			}
		default:
			return null;
		}
	}

}
