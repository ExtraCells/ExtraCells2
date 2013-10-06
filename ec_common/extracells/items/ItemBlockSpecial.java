package extracells.items;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;

public class ItemBlockSpecial extends ItemBlock
{

	public ItemBlockSpecial(int id)
	{
		super(id);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		return StatCollector.translateToLocal(this.getUnlocalizedName(itemstack) + ".name");
	}
}
