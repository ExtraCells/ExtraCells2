package extracells.api;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import appeng.api.storage.ICellWorkbenchItem;

public interface IFluidStorageCell extends ICellWorkbenchItem {

	/**
	 *
	 * @param ItemStack
	 * @return the Fluid Filter. An empty ArrayList or null if the cell accepts
	 *         all Fluids
	 */
	ArrayList<Fluid> getFilter(ItemStack is);

	int getMaxBytes(ItemStack is);

	int getMaxTypes(ItemStack is);

}
