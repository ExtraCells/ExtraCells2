package extracells.util;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Util;
import appeng.api.events.GridPatternUpdateEvent;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.util.ICraftingPattern;

public class ECPrivatePatternInventory extends ECPrivateInventory {
	IGridTileEntity gridTE;

	public ECPrivatePatternInventory(String customName, int size,
			int stackLimit, IGridTileEntity gridTE) {
		super(customName, size, stackLimit);
	}

	@Override
	public void onInventoryChanged() {
		if (gridTE != null && gridTE.getGrid() != null) {
			MinecraftForge.EVENT_BUS.post(new GridPatternUpdateEvent(gridTE
					.getWorld(), gridTE.getLocation(), gridTE.getGrid()));
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		ICraftingPattern currentPattern = Util.getAssemblerPattern(itemstack);
		if (currentPattern == null)
			return false;
		for (ItemStack entry : currentPattern.getRequirements()) {
			if (entry != null && entry.getItem() instanceof IFluidContainerItem
					|| FluidContainerRegistry.isFilledContainer(entry))
				return true;
		}
		return false;
	}
}
