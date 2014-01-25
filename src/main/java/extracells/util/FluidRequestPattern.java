package extracells.util;

import appeng.api.me.util.ICraftingPattern;
import appeng.api.me.util.ITileCraftingProvider;
import com.google.common.collect.Lists;
import extracells.ItemEnum;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class FluidRequestPattern implements ICraftingPattern
{
	ITileCraftingProvider provider;
	FluidStack stack;

	public FluidRequestPattern(ITileCraftingProvider provider, FluidStack stack)
	{
		this.provider = provider;
		this.stack = stack;
	}

	@Override
	public ItemStack getOutput()
	{
		return new ItemStack(ItemEnum.FLUIDDISPLAY.getItemInstance(), stack.amount, stack.fluidID);
	}

	@Override
	public List<ItemStack> getRequirements()
	{
		return null;
	}

	@Override
	public List<ITileCraftingProvider> getProviders()
	{
		return Lists.newArrayList(provider);
	}

	@Override
	public void addProviders(ITileCraftingProvider a)
	{
	}
}
