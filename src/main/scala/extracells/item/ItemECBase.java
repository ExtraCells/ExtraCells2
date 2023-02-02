package extracells.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

class ItemECBase extends Item {

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List tooltip, boolean p_77624_4_) {
        tooltip.add(EnumChatFormatting.RED + "EC2 is going to be removed in future!");
        tooltip.add(EnumChatFormatting.RED + "Try to put it in crafting table to covert it into AE2/AE2FC device.");
    }

}
