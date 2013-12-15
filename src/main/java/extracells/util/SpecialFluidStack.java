package extracells.util;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class SpecialFluidStack
{
	public long amount;
	Fluid fluid;

	public SpecialFluidStack(Fluid fluid, long amount)
	{
		this.fluid = fluid;
		this.amount = amount;
	}

	public SpecialFluidStack(int id, long amount)
	{
		this.fluid = FluidRegistry.getFluid(id);
		this.amount = amount;
	}

	public long getAmount()
	{
		return amount;
	}

	public Fluid getFluid()
	{
		return fluid;
	}

	public int getID()
	{
		if (fluid != null)
			return fluid.getID();
		return 0;
	}
}