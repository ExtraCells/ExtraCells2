package extracells.api;

import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.data.IAEItemStack;
import javax.annotation.Nullable;
import net.minecraft.item.ItemStack;

/**
 * Any item which implements this can be treated as an IMEInventory via
 * Util.getCell / Util.isCell It automatically handles the internals and NBT
 * data, which is both nice, and bad for you!
 * <p>
 * Good cause it means you don't have to do anything, bad because you have
 * little to no control over it.
 * <p>
 * This implementation is a mirror of the standard AE2 item cell type, but
 * allows for any amount of types (within reason) and larger byte amounts.
 */
public interface IStorageCellAdvanced extends ICellWorkbenchItem {

    /**
     * It wont work if the return is not a multiple of 8.
     * The limit is ({@link Long#MAX_VALUE}) / 8.
     *
     * @param cellItem item
     * @return number of bytes
     */
    long getBytes(ItemStack cellItem);

    /**
     * Determines the number of bytes used for any type included on the cell.
     *
     * @param cellItem item
     * @return number of bytes
     */
    int getBytesPerType(ItemStack cellItem);

    /**
     * Must be between 1 and 63, indicates how many types you want to store on
     * the item.
     *
     * @param cellItem item
     * @return number of types
     */
    int getTotalTypes(ItemStack cellItem);

    /**
     * Allows you to fine tune which items are allowed on a given cell, if you
     * don't care, just return false; As the handler for this type of cell is
     * still the default cells, the normal AE black list is also applied.
     *
     * @param cellItem          item
     * @param requestedAddition requested addition
     * @return true to preventAdditionOfItem
     */
    boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition);

    /**
     * Allows you to specify if this storage cell can be stored inside other
     * storage cells, only set this for special items like the matter cannon
     * that are not general purpose storage.
     *
     * @return true if the storage cell can be stored inside other storage
     * cells, this is generally false, except for certain situations
     * such as the matter cannon.
     */
    boolean storableInStorageCell();

    /**
     * Allows an item to selectively enable or disable its status as a storage
     * cell.
     *
     * @param i item
     * @return if the ItemStack should behavior as a storage cell.
     */
    boolean isStorageCell(ItemStack i);

    /**
     * @param i item
     * @return drain in ae/t this storage cell will use.
     */
    double getIdleDrain(@Nullable ItemStack i);
}
