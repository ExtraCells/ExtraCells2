package extracells.util;

import appeng.api.AEApi;
import appeng.api.storage.data.IAEFluidStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;
import org.apache.commons.lang3.tuple.MutablePair;

public class FluidUtil
{
	public static FluidStack getFluidFromContainer(ItemStack itemStack)
	{
		FluidStack fluid = null;
		if (itemStack == null)
			return null;

		ItemStack container = itemStack.copy();
		Item item = container.getItem();
		if (item instanceof IFluidContainerItem)
		{
			return ((IFluidContainerItem) item).getFluid(container);
		} else
		{
			return FluidContainerRegistry.getFluidForFilledItem(container);
		}
	}

	public static boolean isFluidContainer(ItemStack itemStack)
	{
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem || FluidContainerRegistry.isContainer(itemStack))
		{
			return true;
		}
		return false;
	}

	public static MutablePair<Integer, ItemStack> fillStack(ItemStack itemStack, FluidStack fluid, boolean doFill)
	{
		if (itemStack == null)
			return null;
		if (doFill)
			itemStack = itemStack.copy();

		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem)
		{
			int filled = ((IFluidContainerItem) item).fill(itemStack, fluid, doFill);
			return new MutablePair<Integer, ItemStack>(filled, itemStack);
		} else if (FluidContainerRegistry.isContainer(itemStack))
		{
			FluidContainerRegistry.fillFluidContainer(fluid, itemStack);
			FluidStack filled = FluidContainerRegistry.getFluidForFilledItem(itemStack);
			return new MutablePair<Integer, ItemStack>(filled != null ? filled.amount : 0, itemStack);
		}

		return null;
	}

	public static MutablePair<Integer, ItemStack> drainStack(ItemStack itemStack, FluidStack fluid, boolean doDrain)
	{
		if (itemStack == null)
			return null;
		if (doDrain)
			itemStack = itemStack.copy();

		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem)
		{
			FluidStack drained = ((IFluidContainerItem) item).drain(itemStack, fluid.amount, doDrain);
			int amountDrained = drained != null && drained.getFluid() == fluid.getFluid() ? drained.amount : 0;
			return new MutablePair<Integer, ItemStack>(amountDrained, itemStack);
		} else if (FluidContainerRegistry.isContainer(itemStack))
		{
			FluidStack content = FluidContainerRegistry.getFluidForFilledItem(itemStack);
			int amountDrained = content != null && content.getFluid() == fluid.getFluid() ? content.amount : 0;
			return new MutablePair<Integer, ItemStack>(amountDrained, itemStack.getItem().getContainerItem(itemStack));
		}

		return null;
	}

	public static IAEFluidStack createAEFluidStack(FluidStack fluid)
	{
		return AEApi.instance().storage().createFluidStack(new FluidStack(fluid, 1));
	}

	public static IAEFluidStack createAEFluidStack(Fluid fluid, long amount)
	{
		return createAEFluidStack(new FluidStack(fluid, 1)).setStackSize(amount);
	}
}
