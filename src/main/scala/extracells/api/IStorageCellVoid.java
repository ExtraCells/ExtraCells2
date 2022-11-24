package extracells.api;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;

// Copy of IStorageCell
public interface IStorageCellVoid extends ICellWorkbenchItem {

    int getBytes(ItemStack cellItem);

    int getBytesPerType(ItemStack cellItem);

    int getTotalTypes(ItemStack cellItem);

    boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition);

    boolean storableInStorageCell();

    boolean isStorageCell(ItemStack i);

    default double getIdleDrain(@Nullable ItemStack i) {
        // provided for API backwards compatibility
        return getIdleDrain();
    }

    double getIdleDrain();
}
