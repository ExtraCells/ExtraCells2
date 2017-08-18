package extracells.item;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import extracells.registries.ItemEnum;

public class ItemFluidPattern extends ItemECBase {

	public static Fluid getFluid(ItemStack itemStack) {
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		return FluidRegistry.getFluid(itemStack.getTagCompound().getString("fluidID"));
	}

	public static ItemStack getPatternForFluid(Fluid fluid) {
		ItemStack itemStack = new ItemStack(ItemEnum.FLUIDPATTERN.getItem(), 1);
		itemStack.setTagCompound(new NBTTagCompound());
		if (fluid != null)
			itemStack.getTagCompound().setString("fluidID", fluid.getName());
		return itemStack;
	}

	public ItemFluidPattern() {
		setMaxStackSize(1);
		setHasSubtypes(true);
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack) {
		Fluid fluid = getFluid(itemStack);
		if (fluid == null)
			return I18n.translateToLocal(getUnlocalizedName(itemStack));
		return I18n.translateToLocal(getUnlocalizedName(itemStack))
				+ ": " + fluid.getLocalizedName(new FluidStack(fluid, 1));
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.UNCOMMON;
	}

	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
			ItemStack itemStack = new ItemStack(this);
			itemStack.setTagInfo("fluidID", new NBTTagString(fluid.getName()));
			itemList.add(itemStack);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.fluid.pattern";
	}

	@Override
	public ActionResult onItemRightClick(ItemStack itemStack, World world, EntityPlayer entityPlayer, EnumHand hand) {
		if (entityPlayer.isSneaking())
			return new ActionResult(EnumActionResult.SUCCESS, ItemEnum.FLUIDPATTERN.getSizedStack(itemStack.stackSize));
		return new ActionResult(EnumActionResult.SUCCESS, itemStack);
	}
}
