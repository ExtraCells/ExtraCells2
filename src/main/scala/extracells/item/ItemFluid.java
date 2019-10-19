package extracells.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.Constants;
import extracells.models.IItemModelRegister;
import extracells.models.ModelManager;

public class ItemFluid extends Item implements IItemModelRegister {

	public ItemFluid() {
	}

//	@Nullable
//	@Override
//	public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
//		return new ICapabilityProvider() {
//			@Override
//			public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
//				return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
//			}
//
//			@Nullable
//			@Override
//			public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
//				return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(new FluidHandler(stack));
//			}
//		};
//	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		manager.registerItemModel(item, (i) -> new ModelResourceLocation(Constants.MOD_ID + ":fluid/" + getFluidName(i), "inventory"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
		if (!this.isInCreativeTab(tab))
			return;
		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			ItemStack itemStack = new ItemStack(this);
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

//	private static class FluidHandler implements IFluidHandlerItem {
//
//		private ItemStack container;
//
//		public FluidHandler(ItemStack container) {
//			this.container = container;
//		}
//
//		@Nonnull
//		@Override
//		public ItemStack getContainer() {
//			return this.container;
//		}
//
//		@Override
//		public IFluidTankProperties[] getTankProperties() {
//			return new IFluidTankProperties[]{new FluidTankProperties(this.drain(1000, true), 1000, false, false)};
//		}
//
//		@Override
//		public int fill(FluidStack resource, boolean doFill) {
//			return 0;
//		}
//
//		@Nullable
//		@Override
//		public FluidStack drain(FluidStack resource, boolean doDrain) {
//			return null;
//		}
//
//		@Nullable
//		@Override
//		public FluidStack drain(int maxDrain, boolean doDrain) {
//			return null;
//		}
//	}
}
