package extracells;

import java.util.Comparator;

import net.minecraftforge.fluids.FluidStack;

public class ComparatorName implements Comparator<SpecialFluidStack>
{
	boolean reverse = false;

	public ComparatorName(boolean reverse)
	{
		this.reverse = reverse;
	}

	@Override
	public int compare(SpecialFluidStack e, SpecialFluidStack o)
	{
		if (e == null)
		{
			if (!reverse)
			{
				return o == null ? 0 : -1;
			} else
			{
				return o == null ? 0 : 1;
			}
		}
		if (o == null)
			if (!reverse)
			{
				return -1;
			} else
			{
				return 1;
			}
		if (!reverse)
		{
			return e.getFluid().getLocalizedName().compareToIgnoreCase(o.getFluid().getLocalizedName());
		} else
		{
			return -1 * e.getFluid().getLocalizedName().compareToIgnoreCase(o.getFluid().getLocalizedName());
		}
	}
}
