package extracells.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.util.DeprecationWarning;

class ItemECBase extends Item {

    @Override
    @SideOnly(Side.CLIENT)
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack p_77624_1_, EntityPlayer p_77624_2_, List tooltip, boolean p_77624_4_) {
        DeprecationWarning.addGeneralDeprecationWarning(tooltip);
    }

}
