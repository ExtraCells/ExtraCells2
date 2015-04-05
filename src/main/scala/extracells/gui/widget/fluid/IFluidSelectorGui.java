package extracells.gui.widget.fluid;

import appeng.api.storage.data.IAEFluidStack;

public interface IFluidSelectorGui extends IFluidWidgetGui {

	public IFluidSelectorContainer getContainer();

	public IAEFluidStack getCurrentFluid();
}
