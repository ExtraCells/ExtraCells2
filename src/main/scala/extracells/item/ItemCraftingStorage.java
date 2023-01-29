package extracells.item;

import static extracells.item.ItemStoragePhysical.suffixes;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

import appeng.block.AEBaseItemBlock;

public class ItemCraftingStorage extends AEBaseItemBlock {

    public ItemCraftingStorage(Block b) {
        super(b);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return String.format("%s.%s", super.getUnlocalizedName(), suffixes[stack.getItemDamage()]);
    }

    @Override
    public int getMetadata(int meta) {
        return meta;
    }
}
