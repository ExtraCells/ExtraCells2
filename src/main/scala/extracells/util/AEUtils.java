package extracells.util;

import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.storage.data.IAEItemStack;

public class AEUtils {

	public static IAEItemStack createItemStack(ItemStack itemStack) {
		return StorageChannels.ITEM().createStack(itemStack);
	}

	public static IAEFluidStack createFluidStack(Fluid fluid) {
		return createFluidStack(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
	}

	public static IAEFluidStack createFluidStack(Fluid fluid, long amount) {
		return createFluidStack(new FluidStack(fluid, 1)).setStackSize(amount);
	}

	public static IAEFluidStack createFluidStack(String fluidName, long amount) {
		return createFluidStack(FluidRegistry.getFluid(fluidName), amount);
	}

	public static IAEFluidStack createFluidStack(FluidStack fluid) {
		return StorageChannels.FLUID().createStack(fluid);
	}

	public static boolean isItemChannel(IStorageChannel channel) {
		return channel == getItemChannel();
	}

	public static boolean isFluidChannel(IStorageChannel channel) {
		return channel == getFluidChannel();
	}

	public static IStorageChannel getItemChannel() {
		return StorageChannels.ITEM();
	}

	public static IStorageChannel getFluidChannel() {
		return StorageChannels.FLUID();
	}

	public static ICellRegistry cell() {
		return AEApi.instance().registries().cell();
	}
}
