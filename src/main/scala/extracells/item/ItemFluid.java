package extracells.item;

import java.util.List;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.Constants;
import extracells.models.IItemModelRegister;
import extracells.models.ModelManager;

public class ItemFluid extends Item implements IItemModelRegister {

	public ItemFluid() {
		//setCreativeTab(null);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		manager.registerItemModel(item, (i) -> new ModelResourceLocation(Constants.MOD_ID + ":fluid/" + getFluidName(i), "inventory"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			ItemStack itemStack = new ItemStack(itemIn);
			setFluidName(itemStack, fluid.getName());
			subItems.add(itemStack);
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String fluidName = getFluidName(stack);
		if (fluidName.isEmpty()) {
			return "null";
		}
		Fluid fluid = FluidRegistry.getFluid(fluidName);

		if (fluid != null) {
			return fluid.getLocalizedName(new FluidStack(fluid, Fluid.BUCKET_VOLUME));
		}
		return "null";
	}

	public static void setFluidName(ItemStack itemStack, String fluidName) {
		itemStack.setTagInfo("fluid", new NBTTagString(fluidName));
	}

	public static String getFluidName(ItemStack itemStack) {
		if (!itemStack.hasTagCompound()) {
			return "";
		}
		NBTTagCompound tagCompound = itemStack.getTagCompound();
		return tagCompound.getString("fluid");
	}


}
