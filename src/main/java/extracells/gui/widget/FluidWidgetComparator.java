package extracells.gui.widget;

import java.util.Comparator;

public class FluidWidgetComparator implements Comparator<AbstractFluidWidget>
{

	@Override
	public int compare(AbstractFluidWidget o1, AbstractFluidWidget o2)
	{
		return o1.getFluid().getLocalizedName().compareTo(o2.getFluid().getLocalizedName());
	}
}
