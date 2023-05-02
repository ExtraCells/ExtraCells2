package extracells.item;

import static extracells.item.ItemStoragePhysical.suffixes;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.block.AEBaseItemBlock;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.util.DeprecationWarning;

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

    @SuppressWarnings("unchecked")
    @Override
    @SideOnly(Side.CLIENT)
    public void addCheckedInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
        DeprecationWarning.addGeneralDeprecationWarning(list);
        super.addCheckedInformation(stack, player, list, par4);
    }
}
