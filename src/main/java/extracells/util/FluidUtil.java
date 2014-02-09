package extracells.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidUtil
{
	public static FluidStack getFluidFromContainer(ItemStack stack)
	{
		FluidStack fluid = null;
		if (stack == null)
			return null;

		ItemStack container = stack.copy();
		Item item = container.getItem();
		if (item instanceof IFluidContainerItem)
		{
			return ((IFluidContainerItem) item).getFluid(container);
		} else
		{
			return FluidContainerRegistry.getFluidForFilledItem(container);
		}
	}
}
