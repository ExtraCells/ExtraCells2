package extracells.item;

import appeng.block.AEBaseItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import static extracells.item.ItemStoragePhysical.suffixes;

public class ItemCraftingStorage extends AEBaseItemBlock {

	public ItemCraftingStorage(Block b) {
		super(b);
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return String.format("%s.%s", super.getUnlocalizedName(), suffixes[stack.getItemDamage()]);
	}

	@Override
    public int getMetadata(int meta){
        return meta;
    }

}
