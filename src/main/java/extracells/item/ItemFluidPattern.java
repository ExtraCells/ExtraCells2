package extracells.item;

import extracells.registries.ItemEnum;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFluidPattern extends Item
{
	@Override
	public void registerIcons(IIconRegister _iconRegister)
	{
	}

	public IIcon getIconIndex(ItemStack itemStack)
	{
		Fluid fluid = getFluid(itemStack);
		if (fluid == null)
			return net.minecraft.init.Blocks.stone.getIcon(0, 0);
		return fluid.getIcon();
	}

	@Override
	public int getSpriteNumber()
	{
		return 0;
	}

	public static Fluid getFluid(ItemStack itemStack)
	{
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());

		return FluidRegistry.getFluid(itemStack.getTagCompound().getString("fluidID"));
	}

	public static ItemStack getPatternForFluid(Fluid fluid)
	{
		ItemStack itemStack = new ItemStack(ItemEnum.FLUIDPATTERN.getItem(), 1);
		itemStack.setTagCompound(new NBTTagCompound());
		if (fluid != null)
			itemStack.getTagCompound().setString("fluidID", fluid.getName());
		return itemStack;
	}
}
