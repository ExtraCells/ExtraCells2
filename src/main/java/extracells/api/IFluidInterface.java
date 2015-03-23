package extracells.api;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;

public interface IFluidInterface {

	public Fluid getFilter(ForgeDirection side);

	public IFluidTank getFluidTank(ForgeDirection side);

	public IInventory getPatternInventory();

	public void setFilter(ForgeDirection side, Fluid fluid);

	public void setFluidTank(ForgeDirection side, FluidStack fluid);

}
