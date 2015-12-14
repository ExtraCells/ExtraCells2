package extracells.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFluid extends ItemECBase {

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		Fluid fluid = FluidRegistry.getFluid(stack.getItemDamage());
		if (fluid == null || fluid.getBlock() == null)
			return "null";
		Item item = Item.getItemFromBlock(fluid.getBlock());
		if (item == null)
			return "null";
		return item.getItemStackDisplayName(new ItemStack(item));
	}
}
