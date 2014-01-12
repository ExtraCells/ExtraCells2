package extracells.util;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ContainerWorkbench;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;
import appeng.api.Util;
import appeng.api.events.GridPatternUpdateEvent;
import appeng.api.me.tiles.IGridTileEntity;
import appeng.api.me.util.ICraftingPattern;
import appeng.api.me.util.ICraftingPatternMAC;

public class ECPrivatePatternInventory extends ECPrivateInventory
{
	IGridTileEntity gridTE;

	public ECPrivatePatternInventory(String customName, int size, int stackLimit, IGridTileEntity gridTE)
	{
		super(customName, size, stackLimit);
		this.gridTE = gridTE;
	}

	@Override
	public void onInventoryChanged()
	{
		super.onInventoryChanged();
		if (gridTE != null && gridTE.getGrid() != null)
		{
			MinecraftForge.EVENT_BUS.post(new GridPatternUpdateEvent(gridTE.getWorld(), gridTE.getLocation(), gridTE.getGrid()));
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack)
	{
		if (gridTE != null)
		{
			ICraftingPattern currentPattern = Util.getAssemblerPattern(itemstack);
			if (currentPattern == null || currentPattern.getRequirements() == null)
				return false;
			if (FluidContainerRegistry.isEmptyContainer(currentPattern.getOutput()))
				return false;

			for (ItemStack entry : currentPattern.getRequirements())
			{
				if (entry != null && entry.getItem() instanceof IFluidContainerItem || FluidContainerRegistry.isFilledContainer(entry))
				{
					return doesRecipeExist((ICraftingPatternMAC) currentPattern);
				}
			}
		}
		return false;
	}

	public boolean doesRecipeExist(ICraftingPatternMAC pattern)
	{
		InventoryCrafting inv = new InventoryCrafting(new ContainerWorkbench(new InventoryPlayer(null), gridTE.getWorld(), 0, 0, 0)
		{
			public void onCraftMatrixChanged(IInventory par1IInventory)
			{
			}
		}, 3, 3);
		for (int i = 0; i < pattern.getCraftingMatrix().length; i++)
		{
			inv.setInventorySlotContents(i, pattern.getCraftingMatrix()[i]);
		}
		ItemStack thing = CraftingManager.getInstance().findMatchingRecipe(inv, gridTE.getWorld());
		return ItemStack.areItemStacksEqual(thing, pattern.getOutput());
	}
}
