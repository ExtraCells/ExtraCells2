package extracells.item;

import javax.annotation.Nullable;
import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStackSimple;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import extracells.registries.ItemEnum;

public class ItemFluidPattern extends ItemECBase {

	@Nullable
	public static Fluid getFluid(ItemStack itemStack) {
		FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);
		if (fluidStack == null) {
			return null;
		}
		return fluidStack.getFluid();
	}

	public static ItemStack getPatternForFluid(Fluid fluid) {
		ItemStack itemStack = new ItemStack(ItemEnum.FLUIDPATTERN.getItem(), 1);
		IFluidHandler fluidHandler = FluidUtil.getFluidHandler(itemStack);
		if (fluidHandler == null) {
			return itemStack;
		}
		fluidHandler.fill(new FluidStack(fluid, Fluid.BUCKET_VOLUME), true);
		return itemStack;
	}

	public ItemFluidPattern() {
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return new FluidHandlerItemStackSimple(stack, Fluid.BUCKET_VOLUME);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack) {
		Fluid fluid = getFluid(itemStack);
		if (fluid == null) {
			return I18n.translateToLocal(getTranslationKey(itemStack));
		}
		return I18n.translateToLocal(getTranslationKey(itemStack))
			+ ": " + fluid.getLocalizedName(new FluidStack(fluid, 1));
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.UNCOMMON;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs creativeTab, NonNullList itemList) {
		if (!this.isInCreativeTab(creativeTab))
			return;
		super.getSubItems(creativeTab, itemList);
		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			itemList.add(getPatternForFluid(fluid));
		}
	}

	@Override
	public String getTranslationKey(ItemStack itemStack) {
		return "extracells.item.fluid.pattern";
	}

	@Override
	public ActionResult onItemRightClick( World world, EntityPlayer entityPlayer, EnumHand hand) {
		ItemStack itemStack = entityPlayer.getHeldItem(hand);
		if (entityPlayer.isSneaking()) {
			return new ActionResult(EnumActionResult.SUCCESS, ItemEnum.FLUIDPATTERN.getSizedStack(itemStack.getCount()));
		}
		return new ActionResult(EnumActionResult.SUCCESS, itemStack);
	}
}
