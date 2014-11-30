package extracells.api.crafting;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEFluidStack;

public interface IFluidCraftingPatternDetails extends ICraftingPatternDetails {

	public IAEFluidStack[] getCondencedFluidInputs();

	public IAEFluidStack[] getFluidInputs();

}
