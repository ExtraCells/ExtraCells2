package extracells.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

import appeng.api.storage.ICellWorkbenchItem;

public interface IPortableStorageCell extends ICellWorkbenchItem {
	boolean hasPower(EntityPlayer player, double amount, ItemStack is);

	boolean usePower(EntityPlayer player, double amount, ItemStack is);
}
