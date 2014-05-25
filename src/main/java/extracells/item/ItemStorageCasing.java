package extracells.item;

import extracells.Extracells;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageCasing extends Item {

    private IIcon[] icons;
    public final String[] suffixes = {"physical", "fluid"};

    public ItemStorageCasing() {
        setMaxDamage(0);
        setHasSubtypes(true);
        setCreativeTab(Extracells.ModTab);
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack) {
        return "extracells.item.storage.casing." + suffixes[itemStack.getItemDamage()];
    }

    @Override
    public IIcon getIconFromDamage(int dmg) {
        int j = MathHelper.clamp_int(dmg, 0, icons.length - 1);
        return icons[j];
    }

    @Override
    public void registerIcons(IIconRegister iconRegister) {
        icons = new IIcon[suffixes.length];

        for (int i = 0; i < suffixes.length; ++i) {
            icons[i] = iconRegister.registerIcon("extracells:" + "storage.casing." + suffixes[i]);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
        for (int j = 0; j < suffixes.length; ++j) {
            itemList.add(new ItemStack(item, 1, j));
        }
    }
}
