package extracells.inventory;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.config.IncludeExclude;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.util.item.AEItemStack;
import appeng.util.prioitylist.FuzzyPriorityList;
import appeng.util.prioitylist.OreFilteredList;
import appeng.util.prioitylist.PrecisePriorityList;

public class VoidCellInventoryHandler extends MEVoidInventoryHandler<IAEItemStack> implements ICellInventoryHandler {

    VoidCellInventoryHandler(final IMEInventory<IAEItemStack> c) {
        super(c, StorageChannel.ITEMS);

        final ICellInventory ci = this.getCellInv();

        if (ci != null) {
            final IInventory upgrades = ci.getUpgradesInventory();
            final IInventory config = ci.getConfigInventory();
            final FuzzyMode fzMode = ci.getFuzzyMode();
            final String filter = ci.getOreFilter();
            // final String filter = "";
            boolean hasInverter = false;
            boolean hasFuzzy = false;
            boolean hasOreFilter = false;

            for (int x = 0; x < upgrades.getSizeInventory(); x++) {
                final ItemStack is = upgrades.getStackInSlot(x);
                if (is != null && is.getItem() instanceof IUpgradeModule) {
                    final Upgrades u = ((IUpgradeModule) is.getItem()).getType(is);
                    if (u != null) {
                        switch (u) {
                            case FUZZY:
                                hasFuzzy = true;
                                break;
                            case INVERTER:
                                hasInverter = true;
                                break;
                            case ORE_FILTER:
                                hasOreFilter = true;
                                break;
                            default:
                        }
                    }
                }
            }
            this.setWhitelist(hasInverter ? IncludeExclude.BLACKLIST : IncludeExclude.WHITELIST);
            if (hasOreFilter && !filter.isEmpty()) {
                this.setPartitionList(new OreFilteredList(filter));
            } else {
                final IItemList<IAEItemStack> priorityList = AEApi.instance().storage().createItemList();
                for (int x = 0; x < config.getSizeInventory(); x++) {
                    final ItemStack is = config.getStackInSlot(x);
                    if (is != null) {
                        priorityList.add(AEItemStack.create(is));
                    }
                }

                if (!priorityList.isEmpty()) {
                    if (hasFuzzy) {
                        this.setPartitionList(new FuzzyPriorityList<IAEItemStack>(priorityList, fzMode));
                    } else {
                        this.setPartitionList(new PrecisePriorityList<IAEItemStack>(priorityList));
                    }
                }
            }
        }
    }

    @Override
    public ICellInventory getCellInv() {
        Object o = this.getInternal();

        if (o instanceof MEPassThrough) {
            o = ((MEPassThrough) o).getInternal();
        }

        return (VoidCellInventory) (o instanceof VoidCellInventory ? o : null);
    }

    @Override
    public boolean isPreformatted() {
        return !this.getPartitionList().isEmpty();
    }

    @Override
    public boolean isFuzzy() {
        return this.getPartitionList() instanceof FuzzyPriorityList;
    }

    @Override
    public IncludeExclude getIncludeExcludeMode() {
        return this.getWhitelist();
    }

    public int getStatusForCell() {
        ICellInventory c = this.getCellInv();
        int val = 0;
        if (c != null) {
            val = this.getCellInv().getStatusForCell();
        }

        if (val == 1 && this.isPreformatted()) {
            val = 2;
        }

        return val;
    }
}
