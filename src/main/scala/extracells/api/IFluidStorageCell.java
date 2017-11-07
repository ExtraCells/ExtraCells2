package extracells.api;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;


public interface IFluidStorageCell extends IStorageCellBase {

	/**
	 * @param ItemStack
	 * @return the Fluid Filter. An empty ArrayList or null if the cell accepts
	 * all Fluids
	 */
	ArrayList<Fluid> getFilter(ItemStack is);

}
