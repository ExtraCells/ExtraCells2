package extracells.container;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import extracells.api.ECApi;
import extracells.util.FluidHelper;
import extracells.util.GasUtil;

public enum StorageType {
	GAS("gas", "Kilo", "Mega", "") {
		@Override
		public boolean isEmpty(ItemStack stack) {
			return GasUtil.isEmpty(stack);
		}

		@Override
		public boolean isFilled(ItemStack stack) {
			return GasUtil.isFilled(stack);
		}

		@Override
		public boolean canSee(FluidStack fluidStack) {
			return ECApi.instance().isGasStack(fluidStack);
		}

		@Override
		public boolean isContainer(ItemStack stack) {
			return GasUtil.isGasContainer(stack);
		}
	},
	FLUID("fluid", "KiloB", "MegaB", "B") {
		@Override
		public boolean isEmpty(ItemStack stack) {
			return FluidHelper.isEmpty(stack);
		}

		@Override
		public boolean isFilled(ItemStack stack) {
			return FluidHelper.isFilled(stack);
		}

		@Override
		public boolean canSee(FluidStack fluidStack) {
			return ECApi.instance().canFluidSeeInTerminal(fluidStack.getFluid());
		}

		@Override
		public boolean isContainer(ItemStack stack) {
			return FluidHelper.isFluidContainer(stack);
		}
	};

	private String name;
	private String kilo;
	private String mega;
	private String buckets;

	StorageType(String name, String kilo, String mega, String buckets) {
		this.name = name;
		this.kilo = kilo;
		this.mega = mega;
		this.buckets = buckets;
	}

	public String getName() {
		return name;
	}

	public String getBuckets() {
		return buckets;
	}

	public String getKilo() {
		return kilo;
	}

	public String getMega() {
		return mega;
	}

	public abstract boolean isEmpty(ItemStack stack);

	public abstract boolean isFilled(ItemStack stack);

	public abstract boolean canSee(FluidStack fluidStack);

	public abstract boolean isContainer(ItemStack stack);
}
