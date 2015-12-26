package extracells.item;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ItemFluid extends ItemECBase {

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		Fluid fluid = FluidRegistry.getFluid(stack.getItemDamage());

		if (fluid != null)
			return fluid.getLocalizedName(new FluidStack(fluid, FluidContainerRegistry.BUCKET_VOLUME));
		return "null";
	}
}
