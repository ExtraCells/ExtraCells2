package extracells.util;

import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class FluidHelper {

	public static Pair<Integer, ItemStack> drainStack(ItemStack itemStack, FluidStack fluid) {
		if (itemStack == null) {
			return null;
		}
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler == null) {
			return null;
		}
		FluidStack drained = fluidHandler.drain(fluid, true);
		if(fluidHandler instanceof FluidBucketWrapper)
			itemStack = ((FluidBucketWrapper)fluidHandler).getContainer();
		return new MutablePair(drained != null && drained.getFluid() == fluid.getFluid() ? drained.amount : 0, itemStack);
	}

	public static Pair<Integer, ItemStack> fillStack(ItemStack itemStack, FluidStack fluid) {
		if (itemStack == null) {
			return null;
		}
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler == null) {
			return null;
		}
		int filled = fluidHandler.fill(fluid, true);
		if(fluidHandler instanceof FluidBucketWrapper)
			itemStack = ((FluidBucketWrapper)fluidHandler).getContainer();
		return new MutablePair(filled, itemStack);
	}

	public static int getCapacity(ItemStack itemStack) {
		if (itemStack == null) {
			return 0;
		}
		IFluidHandler handler = FluidUtil.getFluidHandler(itemStack);
		int amount = 0;
		if(handler != null){
			for (IFluidTankProperties tank : handler.getTankProperties()) {
				amount += tank.getCapacity();
			}
		}
		return amount;
	}

	public static int getCapacity(ItemStack itemStack, Fluid fluidToFill) {
		if (itemStack == null) {
			return 0;
		}
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		return fluidHandler.fill(new FluidStack(fluidToFill, Integer.MAX_VALUE), false);
	}

	public static FluidStack getFluidFromContainer(ItemStack itemStack) {
		if (itemStack == null) {
			return null;
		}
		return FluidUtil.getFluidContained(itemStack);
	}

	public static boolean isEmpty(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		return isFillableContainerAndEmpty(itemStack);
	}

	public static boolean isFilled(ItemStack itemStack) {
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler == null) {
			return false;
		}

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (properties.getCapacity() > 0) {
				FluidStack contents = properties.getContents();
				if (contents == null) {
					continue;
				} else if (contents.amount > 0) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isFluidContainer(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		return fluidHandler != null;
	}

	public static ItemStack getEmptyContainer(ItemStack container) {
		ItemStack empty = container.copy();
		empty.setCount(1);
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(empty);
		if (fluidHandler == null) {
			return null;
		}
		if (fluidHandler.drain(Integer.MAX_VALUE, true) != null) {
			return empty;
		}
		return null;
	}

	public static boolean isFillableContainerAndEmpty(ItemStack container) {
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(container);
		if (fluidHandler == null) {
			return false;
		}

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (properties.canFill() && properties.getCapacity() > 0) {
				FluidStack contents = properties.getContents();
				if (contents == null) {
					continue;
				} else if (contents.amount > 0) {
					return false;
				}
			}
		}

		return tankProperties.length > 0;
	}

	public static boolean isFillableContainerWithRoom(ItemStack container) {
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(container);
		if (fluidHandler == null) {
			return false;
		}

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (properties.canFill() && properties.getCapacity() > 0) {
				FluidStack contents = properties.getContents();
				if (contents == null) {
					return true;
				} else if (contents.amount < properties.getCapacity()) {
					return true;
				}
			}
		}

		return false;
	}

	public static boolean isDrainableFilledContainer(ItemStack container) {
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(container);
		if (fluidHandler == null) {
			return false;
		}

		IFluidTankProperties[] tankProperties = fluidHandler.getTankProperties();
		for (IFluidTankProperties properties : tankProperties) {
			if (!properties.canDrain()) {
				return false;
			}

			FluidStack contents = properties.getContents();
			if (contents == null || contents.amount <= 0) {
				return false;
			}
		}

		return fluidHandler.drain(Integer.MAX_VALUE, false) != null;
	}
}
