package extracells.util;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IItemList;
import cpw.mods.fml.common.FMLLog;
import extracells.item.ItemFluidPattern;
import extracells.registries.ItemEnum;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.*;
import org.apache.commons.lang3.tuple.MutablePair;

public class FluidUtil {

	public static IAEFluidStack createAEFluidStack(Fluid fluid) {
		return createAEFluidStack(new FluidStack(fluid,
				FluidContainerRegistry.BUCKET_VOLUME));
	}

	public static IAEFluidStack createAEFluidStack(Fluid fluid, long amount) {
		return createAEFluidStack(fluid.getID(), amount);
	}

	public static IAEFluidStack createAEFluidStack(FluidStack fluid) {
		return AEApi.instance().storage().createFluidStack(fluid);
	}

	public static IAEFluidStack createAEFluidStack(int fluidId, long amount) {
		return createAEFluidStack(new FluidStack(FluidRegistry.getFluid(fluidId), 1)).setStackSize(
				amount);
	}

	/**
	 * Drain fluid from an item
	 * @param itemStack Filled fluid container
	 * @param fluid Fluid type and amount to drain
	 * @return Amount drained and the emptied container
	 */
	public static MutablePair<Integer, ItemStack> drainStack(
			ItemStack itemStack, FluidStack fluid) {
		if (itemStack == null)
			return null;
		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem) {
			FluidStack drained = ((IFluidContainerItem) item).drain(itemStack,
					fluid.amount, true);
			int amountDrained = drained != null
					&& drained.getFluid() == fluid.getFluid() ? drained.amount
					: 0;
			return new MutablePair<Integer, ItemStack>(amountDrained, itemStack);
		} else if (FluidContainerRegistry.isContainer(itemStack)) {
			FluidStack content = FluidContainerRegistry
					.getFluidForFilledItem(itemStack);
			int amountDrained = content != null
					&& content.getFluid() == fluid.getFluid() ? content.amount
					: 0;
			return new MutablePair<Integer, ItemStack>(amountDrained,
				FluidContainerRegistry.drainFluidContainer(itemStack));
		}

		return null;
	}

	/**
	 * Fill an ItemStack with fluid
	 * @param itemStack Item to fill, stackSize must be 1
	 * @param fluid The fluid amount to fill the container with
	 * @return Pair: amount actually filled, the filled ItemStack
	 */
	public static MutablePair<Integer, ItemStack> fillStack(
			ItemStack itemStack, FluidStack fluid) {
		if (itemStack == null || itemStack.stackSize != 1)
			return null;
		Item item = itemStack.getItem();
		//If its a fluid container item instance
		if (item instanceof IFluidContainerItem) {
			//Call the fill method on it.
			int filled = ((IFluidContainerItem) item).fill(itemStack, fluid,
					true);

			//Return the filled itemstack.
			return new MutablePair<Integer, ItemStack>(filled, itemStack);
		} else if (FluidContainerRegistry.isContainer(itemStack)) {
			//Fill it through the fluidcontainer registry.
			ItemStack filledContainer = FluidContainerRegistry
					.fillFluidContainer(fluid, itemStack);
			//get the filled fluidstack.
			FluidStack filled = FluidContainerRegistry
					.getFluidForFilledItem(filledContainer);
			//Return filled container and fill amount.
			return new MutablePair<Integer, ItemStack>(
					filled != null ? filled.amount : 0, filledContainer);

		} else if (item == ItemEnum.FLUIDPATTERN.getItem()) {
			return new MutablePair<Integer, ItemStack>(0,
					ItemFluidPattern.getPatternForFluid(fluid.getFluid()));
		}

		return null;
	}

	/**
	 * Try to fill a liquid container item from AE fluid storage. Make sure to SIMULATE first.
	 * @param container Item to fill, stackSize must be 1
	 * @param request Requested fluid amount, the real amount drained may be smaller
	 * @param monitor The AE network
	 * @param mode Whether to simulate or actually move liquids
	 * @param src Action source for auditing
	 * @return Null if failed, otherwise the filled container and the fluid stack it was filled with
	 */
	public static MutablePair<ItemStack, FluidStack> fillItemFromAe(ItemStack container, FluidStack request, IMEMonitor<IAEFluidStack> monitor, Actionable mode, BaseActionSource src) {
		if (container == null || container.stackSize != 1) {
			return null;
		}
		if (request == null || request.amount <= 0) {
			return null;
		}
		if (monitor == null) {
			return null;
		}
		int capacity = FluidUtil.getCapacity(container, request.getFluid());
		int requestAmount = Integer.min(capacity, request.amount);
		IAEFluidStack result = monitor.extractItems(FluidUtil.createAEFluidStack(request.getFluid(), requestAmount), mode, src);
		if (result == null || result.getStackSize() <= 0) {
			return null;
		}
		if (!result.getFluid().equals(request.getFluid())) {
			FMLLog.severe("[ExtraCells2] ME network returned fluid `%s` when requesting `%s`",
				result.getFluid().getName(), request.getUnlocalizedName());
			return null;
		}
		if (result.getStackSize() > (long) requestAmount) {
			FMLLog.severe("[ExtraCells2] ME network returned %d mB of fluid `%s` when requesting %d mB",
				result.getStackSize(), request.getUnlocalizedName(), requestAmount);
			return null;
		}
		MutablePair<Integer, ItemStack> filledContainer = FluidUtil.fillStack(container.copy(), result.getFluidStack());
		if (filledContainer.getLeft() > (int) result.getStackSize()) {
			FMLLog.severe("[ExtraCells2] %d mB of fluid `%s` filled container `%s` with %d mB: check fluid handler implementation",
				result.getStackSize(), request.getUnlocalizedName(), container.getUnlocalizedName(), filledContainer.getLeft());
			return null;
		}
		if (filledContainer.getLeft() < (int) result.getStackSize() && mode == Actionable.MODULATE) {
			// Couldn't completely fill container, attempt to return to AE
			int remaining = (int)result.getStackSize() - filledContainer.getLeft();
			IAEFluidStack notAdded = monitor.injectItems(FluidUtil.createAEFluidStack(result.getFluid(), remaining), Actionable.MODULATE, src);
			if (notAdded != null && notAdded.getStackSize() > 0) {
				FMLLog.severe("[ExtraCells2] %d mL of fluid `%s` voided when trying to fill container `%s`",
					notAdded.getStackSize(), notAdded.getFluid().getName(), container.getUnlocalizedName());
			}
		}
		return new MutablePair<>(filledContainer.getRight(), new FluidStack(request.getFluid(), filledContainer.getLeft()));
	}

	/**
	 * Try to drain a liquid container item into AE fluid storage. Make sure to SIMULATE first.
	 * @param container Item to drain, stackSize must be 1
	 * @param monitor The AE network
	 * @param mode Whether to simulate or actually move liquids
	 * @param src Action source for auditing
	 * @return Null if failed, otherwise the drained container
	 */
	public static ItemStack drainItemIntoAe(ItemStack container, IMEMonitor<IAEFluidStack> monitor, Actionable mode, BaseActionSource src) {
		if (container == null || container.stackSize != 1) {
			return null;
		}
		if (monitor == null) {
			return null;
		}
		FluidStack fluidStack = FluidUtil.getFluidFromContainer(container);
		MutablePair<Integer, ItemStack> drained = FluidUtil.drainStack(container.copy(), fluidStack);
		fluidStack.amount = drained.getLeft();
		IAEFluidStack notAdded = monitor.injectItems(FluidUtil.createAEFluidStack(fluidStack), mode, src);
		if (notAdded == null || notAdded.getStackSize() <= 0) {
			return drained.getRight();
		}
		if (!notAdded.getFluid().equals(fluidStack.getFluid())) {
			FMLLog.severe("[ExtraCells2] ME network returned fluid `%s` when injecting `%s`",
				notAdded.getFluid().getName(), fluidStack.getUnlocalizedName());
			return null;
		}
		if (notAdded.getStackSize() > (long) fluidStack.amount) {
			FMLLog.severe("[ExtraCells2] ME network returned %d mB of fluid `%s` when injecting %d mB",
				notAdded.getStackSize(), fluidStack.getUnlocalizedName(), fluidStack.amount);
			return null;
		}
		fluidStack.amount -= (int) notAdded.getStackSize();
		MutablePair<Integer, ItemStack> partiallyDrained = FluidUtil.drainStack(container.copy(), fluidStack);
		if (partiallyDrained.getLeft() > fluidStack.amount) {
			FMLLog.severe("[ExtraCells2] Voided fluid: %d mB of fluid `%s` drained from container `%s` with %d mB: check fluid handler implementation",
				notAdded.getStackSize(), fluidStack.getUnlocalizedName(), container.getUnlocalizedName(), partiallyDrained.getLeft());
			// Void fluid, but at least don't void the container
			// This generally shouldn't happen, but might for certain configurations of many storage buses pointing at the same almost-full tank
			// when emptying items with a fixed fluid capacity that can't be partially emptied
			return partiallyDrained.getRight();
		}
		if (partiallyDrained.getLeft() < fluidStack.amount) {
			// Couldn't completely empty container, attempt to drain AE to avoid duping fluid
			if (mode == Actionable.MODULATE) {
				int duped = fluidStack.amount - partiallyDrained.getLeft();
				IAEFluidStack extracted = monitor.extractItems(FluidUtil.createAEFluidStack(notAdded.getFluid(), duped), Actionable.MODULATE, src);
				if (extracted == null || extracted.getStackSize() < duped) {
					FMLLog.severe("[ExtraCells2] %d mL of fluid `%s` duped when trying to empty container `%s`",
						extracted == null ? duped : (duped - extracted.getStackSize()),
						notAdded.getFluid().getName(), container.getUnlocalizedName());
				}
			} else {
				return null;
			}
		}
		return partiallyDrained.getRight();
	}

	public static int getCapacity(ItemStack itemStack, Fluid fluid) {
		if (itemStack == null)
			return 0;
		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem) {
			IFluidContainerItem fluidContainerItem = (IFluidContainerItem) item;
			int capacity = fluidContainerItem.getCapacity(itemStack);
			FluidStack existing = fluidContainerItem.getFluid(itemStack);
			if (existing != null) {
				if (!existing.getFluid().equals(fluid)) {
					return 0;
				}
				capacity -= existing.amount;
			}
			return capacity;
		} else if (FluidContainerRegistry.isContainer(itemStack)) {
			return FluidContainerRegistry.getContainerCapacity(new FluidStack(fluid, Integer.MAX_VALUE), itemStack);
		}
		return 0;
	}

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

	public static boolean isEmpty(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem) {
			FluidStack content = ((IFluidContainerItem) item)
					.getFluid(itemStack);
			return content == null || content.amount <= 0;
		}
		return item == ItemEnum.FLUIDPATTERN.getItem()
				|| FluidContainerRegistry.isEmptyContainer(itemStack);
	}

	public static boolean isFilled(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		if (item instanceof IFluidContainerItem) {
			FluidStack content = ((IFluidContainerItem) item)
					.getFluid(itemStack);
			return content != null && content.amount > 0;
		}
		return FluidContainerRegistry.isFilledContainer(itemStack);
	}

	public static boolean isFluidContainer(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		Item item = itemStack.getItem();
		return item instanceof IFluidContainerItem
				|| item == ItemEnum.FLUIDPATTERN.getItem()
				|| FluidContainerRegistry.isContainer(itemStack);
	}

	public static String formatFluidAmount(long millibuckets, boolean forceLongForm) {
		if (!forceLongForm) {
			if (millibuckets >= 1_000_000_000_000L)
				return String.format("%.1fG",millibuckets / 1_000_000_000_000.0);
			else if (millibuckets >= 1_000_000_000L)
				return String.format("%.1fM",millibuckets / 1_000_000_000.0);
			else if (millibuckets >= 1_000_000L)
				return String.format("%.1fk", millibuckets / 1_000_000.0);
			else if (millibuckets >= 1_000L) {
				return String.format("%.1f", millibuckets / 1_000.0);
			}
		}
		return String.format("%,d%s", millibuckets, forceLongForm ? " mB" : "m");
	}

	public static IItemList<IAEFluidStack> filterEmptyFluid(IItemList<IAEFluidStack> _fluidStackList){
		IItemList<IAEFluidStack> temp = AEApi.instance().storage().createFluidList();
		for(IAEFluidStack fluid: _fluidStackList){
			if(fluid.getStackSize() > 0)
				temp.add(fluid);
		}
		return temp;
	}

	public static String formatFluidAmount(long millibuckets) {
		return formatFluidAmount(millibuckets, false);
	}
}
