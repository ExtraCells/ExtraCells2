package extracells.item.storage;

import net.minecraft.item.ItemStack;

import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

import appeng.api.implementations.items.IStorageCell;
import appeng.api.storage.data.IAEFluidStack;

import extracells.inventory.ECFluidFilterInventory;
import extracells.inventory.InventoryPlain;
import extracells.util.StorageChannels;

public class ItemStorageCellFluid extends ItemStorageCell<IAEFluidStack> implements IStorageCell<IAEFluidStack> {

    public ItemStorageCellFluid() {
        super(CellDefinition.FLUID, StorageChannels.FLUID());
    }

    @Override
    public IItemHandler getConfigInventory(ItemStack is) {
        return new InvWrapper(new ECFluidFilterInventory("configFluidCell", 63, is));
    }

    @Override
    public IItemHandler getUpgradesInventory(ItemStack is) {
        return new InvWrapper(new InventoryPlain("configInventory", 0, 64));
    }
}
