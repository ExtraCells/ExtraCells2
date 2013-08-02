package extracells.items;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockSpecial extends ItemBlock
{

	public ItemBlockSpecial(int id)
	{
		super(id);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		return this.getLocalizedName(itemstack);
	}

}
