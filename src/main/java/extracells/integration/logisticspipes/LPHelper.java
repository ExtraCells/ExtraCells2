package extracells.integration.logisticspipes;

import appeng.api.IAEItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

import java.lang.reflect.Field;

public class LPHelper
{
	private Item LPFluidItem;
	private boolean LPInstalled;

	public LPHelper()
	{
		try
		{
			Class LPMain = Class.forName("logisticspipes.LogisticsPipes");
			Field field = LPMain.getDeclaredField("LogisticsFluidContainer");
			LPFluidItem = (Item) field.get(Item.appleGold);
		} catch (Throwable e)
		{
			LPInstalled = false;
		}
		LPInstalled = LPFluidItem != null;
	}

	public boolean isLPInstalled()
	{
		return LPInstalled;
	}

	public ItemStack makeLPItem(FluidStack stack)
	{
		ItemStack item = new ItemStack(LPFluidItem, 1);
		NBTTagCompound nbt = new NBTTagCompound("tag");
		stack.writeToNBT(nbt);
		item.setTagCompound(nbt);
		return item;
	}

	public ItemStack makeLPItem(IAEItemStack stack)
	{
		return makeLPItem(new FluidStack(stack.getItemDamage(), (int) stack.getStackSize()));
	}

	public FluidStack getFluidFromItem(ItemStack stack)
	{
		if (!stack.hasTagCompound())
			return null;
		NBTTagCompound nbt = stack.getTagCompound().getCompoundTag("nbt");
		return FluidStack.loadFluidStackFromNBT(nbt);
	}
}
