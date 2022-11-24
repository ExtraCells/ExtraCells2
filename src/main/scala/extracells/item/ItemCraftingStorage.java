package extracells.item;

import static extracells.item.ItemStoragePhysical.suffixes;

import appeng.block.AEBaseItemBlock;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;

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
