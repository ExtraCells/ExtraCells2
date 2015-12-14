package extracells.gui.widget;

import extracells.gui.widget.fluid.AbstractFluidWidget;
import net.minecraftforge.fluids.FluidStack;

import java.util.Comparator;

public class FluidWidgetComparator implements Comparator<AbstractFluidWidget> {

	@Override
	public int compare(AbstractFluidWidget o1, AbstractFluidWidget o2) {
		return o1.getFluid().getLocalizedName(new FluidStack(o1.getFluid(), 0))
				.compareTo(o2.getFluid().getLocalizedName(new FluidStack(o1.getFluid(), 0)));
	}
}
