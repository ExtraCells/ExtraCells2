package extracells.container;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.FluidStack;

import extracells.api.ECApi;
import extracells.util.FluidUtil;
import extracells.util.GasUtil;

public enum TerminalType {
	GAS("gas"){
		@Override
		public boolean isEmpty(ItemStack stack){
			return GasUtil.isEmpty(stack);
		}

		@Override
		public boolean isFilled(ItemStack stack){
			return GasUtil.isFilled(stack);
		}

		@Override
		public boolean canSee(FluidStack fluidStack) {
			return ECApi.instance().isGasStack(fluidStack);
		}
	},
	FLUID("fluid"){
		@Override
		public boolean isEmpty(ItemStack stack){
			return FluidUtil.isEmpty(stack);
		}

		@Override
		public boolean isFilled(ItemStack stack){
			return FluidUtil.isFilled(stack);
		}

		@Override
		public boolean canSee(FluidStack fluidStack) {
			return ECApi.instance().canFluidSeeInTerminal(fluidStack.getFluid());
		}
	};

	String name;

	TerminalType(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract boolean isEmpty(ItemStack stack);

	public abstract boolean isFilled(ItemStack stack);

	public abstract boolean canSee(FluidStack fluidStack);
}
