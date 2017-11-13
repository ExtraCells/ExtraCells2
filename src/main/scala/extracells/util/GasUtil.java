package extracells.util;

import appeng.api.storage.data.IItemList;
import extracells.api.gas.IAEGasStack;
import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.data.IAEFluidStack;
import extracells.integration.mekanism.gas.MekanismGas;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import mekanism.api.gas.IGasItem;

public class GasUtil {

	public static IAEFluidStack createAEFluidStack(Gas gas) {
		return createAEFluidStack(new FluidStack(MekanismGas.getFluidGasMap().get(gas),1000));
	}

	public static IAEFluidStack createAEFluidStack(GasStack gasStack) {
		return gasStack == null ? null : createAEFluidStack(new FluidStack(MekanismGas.getFluidGasMap().get(gasStack.getGas()), gasStack.amount));
	}


	public static IAEFluidStack createAEFluidStack(FluidStack fluid) {
		return StorageChannels.FLUID().createStack(fluid);
	}

	public static IAEFluidStack createAEFluidStack(Gas gas, long amount) {
		return createAEFluidStack(new FluidStack(MekanismGas.getFluidGasMap().get(gas), 1)).setStackSize(
			amount);
	}

	public static IAEFluidStack createAEFluidStack(IAEGasStack gasStack){
		if (gasStack == null || gasStack.getGas() == null)
			return null;
		return createAEFluidStack((Gas)gasStack.getGas()).setStackSize(gasStack.getStackSize());
	}

	public static IAEGasStack createAEGasStack(Gas gas) {
		return StorageChannels.GAS().createStack(gas);
	}

	public static IAEGasStack createAEGasStack(GasStack gasStack) {
		return StorageChannels.GAS().createStack(gasStack);
	}

	public static IAEGasStack createAEGasStack(Gas gas, long amount) {
		return createAEGasStack(gas).setStackSize(amount);
	}

	public static IAEGasStack createAEGasStack(IAEFluidStack fluidStack) {
		if (fluidStack == null || fluidStack.getFluid() == null || !isGas(fluidStack.getFluid()))
			return null;
		return createAEGasStack(getGas(fluidStack.getFluid())).setStackSize(fluidStack.getStackSize());
	}

	public static IItemList<IAEGasStack> createAEGasItemList(IItemList<IAEFluidStack> fluidStacks){
		IItemList<IAEGasStack> out = StorageChannels.GAS().createList();
		for (IAEFluidStack fluid : fluidStacks) {
			out.add(createAEGasStack(fluid));
		}
		return out;
	}

	public static IItemList<IAEFluidStack> createAEFluidItemList(IItemList<IAEGasStack> fluidStacks){
		IItemList<IAEFluidStack> out = StorageChannels.FLUID().createList();
		for (IAEGasStack gas : fluidStacks) {
			out.add(createAEFluidStack(gas));
		}
		return out;
	}

	public static MutablePair<Integer, ItemStack> drainStack(ItemStack itemStack, GasStack gas) {
		if (itemStack == null) {
			return null;
		}
		Item item = itemStack.getItem();
		if (item instanceof IGasItem) {
			GasStack drained = ((IGasItem) item).removeGas(itemStack,
				gas.amount);
			int amountDrained = drained != null
				&& drained.getGas() == gas.getGas() ? drained.amount
				: 0;
			return new MutablePair<Integer, ItemStack>(amountDrained, itemStack);
		}
		return null;
	}

	public static MutablePair<Integer, ItemStack> fillStack(
		ItemStack itemStack, GasStack gas) {
		if (itemStack == null) {
			return null;
		}
		Item item = itemStack.getItem();
		if (item instanceof IGasItem) {
			int filled = ((IGasItem) item).addGas(itemStack, gas);
			return new MutablePair<Integer, ItemStack>(filled, itemStack);
		}

		return null;
	}

	public static int getCapacity(ItemStack itemStack) {
		if (itemStack == null) {
			return 0;
		}
		Item item = itemStack.getItem();
		if (item instanceof IGasItem) {
			return ((IGasItem) item).getMaxGas(itemStack);
		}
		return 0;
	}

	public static GasStack getGasFromContainer(ItemStack itemStack) {
		if (itemStack == null) {
			return null;
		}

		ItemStack container = itemStack.copy();
		Item item = container.getItem();
		if (item instanceof IGasItem) {
			return ((IGasItem) item).getGas(itemStack);
		}
		return null;
	}

	public static boolean isEmpty(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		Item item = itemStack.getItem();
		if (item instanceof IGasItem) {
			GasStack content = ((IGasItem) item).getGas(itemStack);
			return content == null || content.amount <= 0;
		}
		return false;
	}

	public static boolean isFilled(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		Item item = itemStack.getItem();
		if (item instanceof IGasItem) {
			GasStack content = ((IGasItem) item).getGas(itemStack);
			return content != null && content.amount > 0;
		}
		return FluidHelper.isFilled(itemStack);
	}

	public static boolean isGasContainer(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		Item item = itemStack.getItem();
		return item instanceof IGasItem;
	}

	public static GasStack getGasStack(FluidStack fluidStack) {
		if (fluidStack == null) {
			return null;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid instanceof MekanismGas.GasFluid) {
			return new GasStack(((MekanismGas.GasFluid) fluid).getGas(), fluidStack.amount);
		}
		return null;
	}

	public static GasStack getGasStack(IAEFluidStack fluidStack) {
		if (fluidStack == null) {
			return null;
		}
		Fluid fluid = fluidStack.getFluid();
		if (fluid instanceof MekanismGas.GasFluid) {
			return new GasStack(((MekanismGas.GasFluid) fluid).getGas(), (int) fluidStack.getStackSize());
		}
		return null;
	}

	public static FluidStack getFluidStack(GasStack gasStack) {
		if (gasStack == null) {
			return null;
		}
		Fluid fluid = MekanismGas.getFluidGasMap().get(gasStack.getGas());
		if (fluid == null) {
			return null;
		}
		return new FluidStack(fluid, gasStack.amount);
	}

	public static boolean isGas(FluidStack fluidStack) {
		return fluidStack != null && isGas(fluidStack.getFluid());
	}

	public static boolean isGas(Fluid fluid) {
		return fluid != null && fluid instanceof MekanismGas.GasFluid;
	}

	public static Gas getGas(Fluid fluid) {
		if (fluid == null) {
			return null;
		}
		if (fluid instanceof MekanismGas.GasFluid) {
			return ((MekanismGas.GasFluid) fluid).getGas();
		}
		return null;
	}
}
