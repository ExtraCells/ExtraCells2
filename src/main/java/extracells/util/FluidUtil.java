package extracells.util;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import extracells.item.ItemFluidPattern;
import extracells.registries.ItemEnum;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.commons.lang3.tuple.MutablePair;

public class FluidUtil {

    public static FluidStack getFluidFromContainer(ItemStack itemStack) {
        if (itemStack == null)
            return null;

        ItemStack container = itemStack.copy();
        Item item = container.getItem();
        if (item instanceof IFluidContainerItem) {
            return ((IFluidContainerItem) item).getFluid(container);
        } else if (item == ItemEnum.FLUIDPATTERN.getItem()) {
            return new FluidStack(ItemFluidPattern.getFluid(itemStack), 0);
        } else {
            return FluidContainerRegistry.getFluidForFilledItem(container);
        }
    }

    public static boolean isFluidContainer(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        Item item = itemStack.getItem();
        return item instanceof IFluidContainerItem || item == ItemEnum.FLUIDPATTERN.getItem() || FluidContainerRegistry.isContainer(itemStack);
    }

    public static boolean isEmpty(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        Item item = itemStack.getItem();
        if (item instanceof IFluidContainerItem) {
            FluidStack content = ((IFluidContainerItem) item).getFluid(itemStack);
            return content == null || content.amount <= 0;
        }
        return item == ItemEnum.FLUIDPATTERN.getItem() || FluidContainerRegistry.isEmptyContainer(itemStack);
    }

    public static boolean isFilled(ItemStack itemStack) {
        if (itemStack == null)
            return false;
        Item item = itemStack.getItem();
        if (item instanceof IFluidContainerItem) {
            FluidStack content = ((IFluidContainerItem) item).getFluid(itemStack);
            return content != null && content.amount <= 0;
        }
        return FluidContainerRegistry.isFilledContainer(itemStack);
    }

    public static int getCapacity(ItemStack itemStack) {
        if (itemStack == null)
            return 0;
        Item item = itemStack.getItem();
        if (item instanceof IFluidContainerItem) {
            return ((IFluidContainerItem) item).getCapacity(itemStack);
        } else if (FluidContainerRegistry.isEmptyContainer(itemStack)) {
            for (FluidContainerRegistry.FluidContainerData data : FluidContainerRegistry.getRegisteredFluidContainerData()) {
                if (data != null && data.emptyContainer.isItemEqual(itemStack)) {
                    FluidStack interior = data.fluid;
                    return interior != null ? interior.amount : 0;
                }
            }
        }
        return 0;
    }

    public static MutablePair<Integer, ItemStack> fillStack(ItemStack itemStack, FluidStack fluid) {
        if (itemStack == null)
            return null;
        Item item = itemStack.getItem();
        if (item instanceof IFluidContainerItem) {
            int filled = ((IFluidContainerItem) item).fill(itemStack, fluid, true);
            return new MutablePair<Integer, ItemStack>(filled, itemStack);
        } else if (FluidContainerRegistry.isContainer(itemStack)) {
            ItemStack filledContainer = FluidContainerRegistry.fillFluidContainer(fluid, itemStack);
            FluidStack filled = FluidContainerRegistry.getFluidForFilledItem(filledContainer);
            return new MutablePair<Integer, ItemStack>(filled != null ? filled.amount : 0, filledContainer);
        } else if (item == ItemEnum.FLUIDPATTERN.getItem()) {
            return new MutablePair<Integer, ItemStack>(0, ItemFluidPattern.getPatternForFluid(fluid.getFluid()));
        }

        return null;
    }

    public static MutablePair<Integer, ItemStack> drainStack(ItemStack itemStack, FluidStack fluid) {
        if (itemStack == null)
            return null;
        Item item = itemStack.getItem();
        if (item instanceof IFluidContainerItem) {
            FluidStack drained = ((IFluidContainerItem) item).drain(itemStack, fluid.amount, true);
            int amountDrained = drained != null && drained.getFluid() == fluid.getFluid() ? drained.amount : 0;
            return new MutablePair<Integer, ItemStack>(amountDrained, itemStack);
        } else if (FluidContainerRegistry.isContainer(itemStack)) {
            FluidStack content = FluidContainerRegistry.getFluidForFilledItem(itemStack);
            int amountDrained = content != null && content.getFluid() == fluid.getFluid() ? content.amount : 0;
            return new MutablePair<Integer, ItemStack>(amountDrained, itemStack.getItem().getContainerItem(itemStack));
        }

        return null;
    }

    public static IAEFluidStack createAEFluidStack(FluidStack fluid) {
        return AEApi.instance().storage().createFluidStack(fluid);
    }

    public static IAEFluidStack createAEFluidStack(Fluid fluid) {
        return createAEFluidStack(new FluidStack(fluid.getID(), FluidContainerRegistry.BUCKET_VOLUME));
    }

    public static IAEFluidStack createAEFluidStack(Fluid fluid, long amount) {
        return createAEFluidStack(fluid.getID(), amount);
    }

    public static IAEFluidStack createAEFluidStack(int fluidId, long amount) {
        return createAEFluidStack(new FluidStack(fluidId, 1)).setStackSize(amount);
    }
}
