package extracells.items;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFluidDisplay extends Item
{

	public ItemFluidDisplay(int id)
	{
		super(id);
		this.setMaxStackSize(Integer.MAX_VALUE);
	}

	@Override
	public Icon getIconFromDamage(int dmg)
	{
		Fluid tFluid = FluidRegistry.getFluid(dmg);
		return tFluid != null ? tFluid.getStillIcon() : Block.stone.getIcon(0, 0);
	}

	public int func_94901_k()
	{
		return 0;
	}

	@Override
	public String getUnlocalizedName()
	{
		return "FluidDisplay - this item is just used to mime fluids!";
	}

	public String getUnlocalizedName(ItemStack aStack)
	{
		if (aStack != null)
		{
			Fluid tFluid = FluidRegistry.getFluid(aStack.getItemDamage());
			if (tFluid != null)
			{
				return tFluid.getLocalizedName();
			}
		}

		return "FluidDisplay - this item is just used to mime fluids!";
	}

	public String getItemStackDisplayName(ItemStack aStack)
	{
		return StatCollector.translateToLocal(this.getUnlocalizedName(aStack));
	}

	public String getItemDisplayName(ItemStack aStack)
	{
		return StatCollector.translateToLocal(this.getUnlocalizedName(aStack));
	}
}
