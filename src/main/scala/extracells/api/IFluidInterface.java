package extracells.api;

import appeng.api.util.AEPartLocation;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidInterface {

	public Fluid getFilter(AEPartLocation side);

	public IFluidTank getFluidTank(AEPartLocation side);

	public IInventory getPatternInventory();

	public void setFilter(AEPartLocation side, Fluid fluid);

	public void setFluidTank(AEPartLocation side, FluidStack fluid);

}
