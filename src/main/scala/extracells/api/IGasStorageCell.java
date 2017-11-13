package extracells.api;

import java.util.ArrayList;

import net.minecraft.item.ItemStack;

import net.minecraftforge.fluids.Fluid;

import appeng.api.storage.ICellWorkbenchItem;

public interface IGasStorageCell extends IStorageCellBase {

	/**
	 * @param ItemStack
	 * @return the Gas Filter. An empty ArrayList or null if the cell accepts
	 * all Gas
	 */
	ArrayList<Object> getFilter(ItemStack is);

}
