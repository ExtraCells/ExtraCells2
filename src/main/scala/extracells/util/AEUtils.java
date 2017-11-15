package extracells.util;

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
		return AEApi.instance().storage().createItemStack(itemStack);
	}

	public static IAEFluidStack createFluidStack(Fluid fluid) {
		return createFluidStack(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
	}

	public static IAEFluidStack createFluidStack(Fluid fluid, long amount) {
		return fluid == null ? null : createFluidStack(new FluidStack(fluid, 1)).setStackSize(amount);
	}

	public static IAEFluidStack createFluidStack(String fluidName, long amount) {
		return createFluidStack(FluidRegistry.getFluid(fluidName), amount);
	}

	public static IAEFluidStack createFluidStack(FluidStack fluid) {
		return fluid == null ? null : AEApi.instance().storage().createFluidStack(fluid);
	}

	public static boolean isItemChannel(StorageChannel channel) {
		return channel == getItemChannel();
	}

	public static boolean isFluidChannel(StorageChannel channel) {
		return channel == getFluidChannel();
	}

	public static StorageChannel getItemChannel() {
		return StorageChannel.ITEMS;
	}

	public static StorageChannel getFluidChannel() {
		return StorageChannel.FLUIDS;
	}

	public static ICellRegistry cell() {
		return AEApi.instance().registries().cell();
	}
}
