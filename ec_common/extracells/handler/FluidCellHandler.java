package extracells.handler;

import net.minecraft.item.ItemStack;
import appeng.api.ICellHandler;
import appeng.api.me.util.IMEInventoryHandler;
import extracells.items.ItemStorageFluid;

public class FluidCellHandler implements ICellHandler
{
	@Override
	public boolean isCell(ItemStack is)
	{
		return is != null ? is.getItem() == extracells.Extracells.StorageFluid : false;
	}

	@Override
	public IMEInventoryHandler getCellHandler(ItemStack itemstack)
	{
		if (itemstack.getItem() == extracells.Extracells.StorageFluid)
		{
			ItemStorageFluid cell = (ItemStorageFluid) itemstack.getItem();
			return new FluidStorageInventoryHandler(itemstack, cell.getBytes(itemstack), cell.getTotalTypes(itemstack));
		}
		return null;
	}
}
