package extracells.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidContainerItem;

public class FluidUtil
{
	public static FluidStack getFluidFromContainer(ItemStack container)
	{
		FluidStack fluid = null;
		if (container == null)
			return null;
		Item item = container.getItem();
		if (item instanceof IFluidContainerItem)
		{
			return ((IFluidContainerItem) item).getFluid(container);
		} else if (FluidContainerRegistry.isFilledContainer(container))
		{
			return FluidContainerRegistry.getFluidForFilledItem(container);
		}
		return null;
	}
}
