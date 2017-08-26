package extracells.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;
import extracells.container.IFluidSelectorContainer;

public interface IFluidSelectorGui extends IFluidWidgetGui {

	IFluidSelectorContainer getContainer();

	IAEFluidStack getCurrentFluid();
}
