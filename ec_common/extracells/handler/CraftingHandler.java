package extracells.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import cpw.mods.fml.common.ICraftingHandler;
import extracells.Extracells;

public class CraftingHandler implements ICraftingHandler
{

	@Override
	public void onCrafting(EntityPlayer player, ItemStack item, IInventory craftMatrix)
	{
		// set size of Adjustable Cell on crafting

		if (item.getItem() == Extracells.StoragePhysical && item.getItemDamage() == 5)
		{
			if (!item.hasTagCompound())
			{
				item.setTagCompound(new NBTTagCompound());
			}
			item.getTagCompound().setInteger("costum_size", 4096);
			item.getTagCompound().setInteger("costum_types", 27);
		}
	}

	@Override
	public void onSmelting(EntityPlayer player, ItemStack item)
	{
		// Not used due nonexistent smelting recipes...
	}

}
