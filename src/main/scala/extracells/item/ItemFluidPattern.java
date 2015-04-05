package extracells.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import extracells.registries.ItemEnum;

public class ItemFluidPattern extends Item {

	public static Fluid getFluid(ItemStack itemStack) {
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		return FluidRegistry.getFluid(itemStack.getTagCompound().getString(
				"fluidID"));
	}

	public static ItemStack getPatternForFluid(Fluid fluid) {
		ItemStack itemStack = new ItemStack(ItemEnum.FLUIDPATTERN.getItem(), 1);
		itemStack.setTagCompound(new NBTTagCompound());
		if (fluid != null)
			itemStack.getTagCompound().setString("fluidID", fluid.getName());
		return itemStack;
	}

	IIcon icon;

	public ItemFluidPattern() {
		setMaxStackSize(1);
	}

	@Override
	public IIcon getIcon(ItemStack itemStack, int pass) {
		switch (pass) {
		case 0:
			Fluid fluid = getFluid(itemStack);
			if (fluid == null)
				return null;
			return fluid.getIcon();
		default:
			return this.icon;
		}
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemStack) {
		Fluid fluid = getFluid(itemStack);
		if (fluid == null)
			return StatCollector
					.translateToLocal(getUnlocalizedName(itemStack));
		return StatCollector.translateToLocal(getUnlocalizedName(itemStack))
				+ ": " + fluid.getLocalizedName(new FluidStack(fluid, 1));
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.uncommon;
	}

	@Override
	public int getSpriteNumber() {
		return 1;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (String fluidID : FluidRegistry.getRegisteredFluidIDs().keySet()) {
			ItemStack itemStack = new ItemStack(this, 1);
			itemStack.setTagCompound(new NBTTagCompound());
			itemStack.getTagCompound().setString("fluidID", fluidID);
			itemList.add(itemStack);
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.fluid.pattern";
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world,
			EntityPlayer entityPlayer) {
		if (entityPlayer.isSneaking())
			return ItemEnum.FLUIDPATTERN.getSizedStack(itemStack.stackSize);
		return itemStack;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icon = iconRegister.registerIcon("extracells:fluid.pattern");
	}

	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}
}
