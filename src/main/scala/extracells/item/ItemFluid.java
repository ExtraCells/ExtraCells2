package extracells.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ItemFluid extends ItemECBase {

	public ItemFluid() {
		setCreativeTab(null);
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String fluidName = getFluidName(stack);
		if(fluidName.isEmpty()){
			return "null";
		}
		Fluid fluid = FluidRegistry.getFluid(fluidName);

		if (fluid != null) {
			return fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
		}
		return "null";
	}

	public static void setFluidName(ItemStack itemStack, String fluidName){
		itemStack.setTagInfo("fluid", new NBTTagString(fluidName));
	}

	public static String getFluidName(ItemStack itemStack){
		if(!itemStack.hasTagCompound()){
			return "";
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		return tagCompound.getString("fluid");
	}

}
