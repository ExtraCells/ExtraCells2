package extracells.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;

public interface IFluidSelectorGui extends IFluidWidgetGui {

	IFluidSelectorContainer getContainer();

	IAEFluidStack getCurrentFluid();
}
