package extracells.container.slot;

import appeng.api.AEApi;
import appeng.api.definitions.Materials;
import appeng.api.implementations.guiobjects.INetworkTool;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotNetworkTool extends Slot
{
	IInventory inventory;

	public SlotNetworkTool(INetworkTool inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
		this.inventory = inventory;
	}

	@Override
	public boolean isItemValid(ItemStack itemstack)
	{
		if (itemstack == null)
			return false;
		Materials materials = AEApi.instance().materials();
		if (materials.materialCardRedstone.sameAs(itemstack))
			return true;
		else if (materials.materialCardSpeed.sameAs(itemstack))
			return true;
		else if (materials.materialCardFuzzy.sameAs(itemstack))
			return true;
		else if (materials.materialCardCapacity.sameAs(itemstack))
			return true;
		else if (materials.materialCardInverter.sameAs(itemstack))
			return true;
		return false;
	}
}
