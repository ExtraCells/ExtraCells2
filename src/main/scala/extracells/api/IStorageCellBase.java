package extracells.api;


import appeng.api.storage.ICellWorkbenchItem;
import net.minecraft.item.ItemStack;

public interface IStorageCellBase extends ICellWorkbenchItem {

    int getMaxBytes(ItemStack is);

    int getMaxTypes(ItemStack is);
}
