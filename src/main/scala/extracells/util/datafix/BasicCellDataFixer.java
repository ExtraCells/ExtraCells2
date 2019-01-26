package extracells.util.datafix;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;

import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.channels.IFluidStorageChannel;
import appeng.api.storage.data.IAEFluidStack;

import com.google.common.base.Preconditions;
import extracells.api.gas.IAEGasStack;
import extracells.api.gas.IGasStorageChannel;
import extracells.util.Log;
import extracells.util.StorageChannels;
import mekanism.api.gas.GasStack;

/**
 * @author BrockWS
 */
public class BasicCellDataFixer implements IFixableData {

    @Override
    public int getFixVersion() {
        return 4;
    }

    @Override
    @Nonnull
    public NBTTagCompound fixTagCompound(@Nonnull NBTTagCompound oldItem) {
        String id = oldItem.getString("id");
        if (!id.equalsIgnoreCase("extracells:storage.fluid") && !id.equalsIgnoreCase("extracells:storage.gas"))
            return oldItem;
        if (!oldItem.hasKey("tag") || oldItem.getCompoundTag("tag").hasNoTags())
            return oldItem;

        boolean isFluid = id.contains("fluid");

        boolean needsConvert = false;
        for (int i = 0; i < 5; i++)
            if (oldItem.getCompoundTag("tag").hasKey((isFluid ? "Fluid#" : "Gas#") + i))
                needsConvert = true;

        if (!needsConvert)
            return oldItem;

        NBTTagCompound item = isFluid ? this.fixFluidCell(oldItem) : this.fixGasCell(oldItem);

        Log.info("Converted tag for {} cell from {} to {}", isFluid ? "fluid" : "gas", oldItem, item);
        return item;
    }

    protected NBTTagCompound fixFluidCell(NBTTagCompound oldItem) {
        NBTTagCompound item = oldItem.copy();
        NBTTagCompound tag = item.getCompoundTag("tag");
        IFluidStorageChannel channel = StorageChannels.FLUID();

        ICellInventoryHandler<IAEFluidStack> cellInventory = AEApi.instance().registries().cell().getCellInventory(new ItemStack(item), null, channel);
        Preconditions.checkNotNull(cellInventory);

        for (int i = 0; i < 5; i++) {
            if (!tag.hasKey("Fluid#" + i))
                continue;
            FluidStack fluidStack = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag("Fluid#" + i));
            Preconditions.checkNotNull(fluidStack, "Failed to read FluidStack " + tag.getCompoundTag("Fluid#" + i));
            IAEFluidStack stack = channel.createStack(fluidStack);
            if (stack == null)
                continue;
            cellInventory.injectItems(stack, Actionable.MODULATE, null);
            tag.removeTag("Fluid#" + i);
        }
        return item;
    }

    protected NBTTagCompound fixGasCell(NBTTagCompound oldItem) {
        NBTTagCompound item = oldItem.copy();
        NBTTagCompound tag = item.getCompoundTag("tag");
        IGasStorageChannel channel = StorageChannels.GAS();

        ICellInventoryHandler<IAEGasStack> cellInventory = AEApi.instance().registries().cell().getCellInventory(new ItemStack(item), null, channel);
        Preconditions.checkNotNull(cellInventory);

        for (int i = 0; i < 5; i++) {
            if (!tag.hasKey("Gas#" + i))
                continue;
            GasStack gasStack = GasStack.readFromNBT(tag.getCompoundTag("Gas#" + i));
            Preconditions.checkNotNull(gasStack, "Failed to read GasStack " + tag.getCompoundTag("Gas#" + i));
            IAEGasStack stack = channel.createStack(gasStack);
            if (stack == null)
                continue;
            cellInventory.injectItems(stack, Actionable.MODULATE, null);
            tag.removeTag("Gas#" + i);
        }
        return item;
    }
}
