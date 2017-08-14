package extracells.api;

import net.minecraft.inventory.IInventory;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

import appeng.api.util.AEPartLocation;

public interface IFluidInterface {

	Fluid getFilter(AEPartLocation side);

	IFluidTank getFluidTank(AEPartLocation side);

	IInventory getPatternInventory();

	void setFilter(AEPartLocation side, Fluid fluid);

	void setFluidTank(AEPartLocation side, FluidStack fluid);

}
