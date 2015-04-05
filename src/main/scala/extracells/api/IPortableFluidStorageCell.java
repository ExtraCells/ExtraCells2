package extracells.api;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IPortableFluidStorageCell extends IFluidStorageCell {

	public boolean hasPower(EntityPlayer player, double amount, ItemStack is);

	public boolean usePower(EntityPlayer player, double amount, ItemStack is);

}
