package extracells.item;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ItemFluid extends Item {
	
	public String getItemStackDisplayName(ItemStack stack)
    {
		Fluid fluid = FluidRegistry.getFluid(stack.getItemDamage());
		if(fluid == null || fluid.getBlock() == null)
			return "null";
		Item item = Item.getItemFromBlock(fluid.getBlock());
		if(item == null)
			return "null";
		return item.getItemStackDisplayName(new ItemStack(item));
    }
	
	@SideOnly(Side.CLIENT)
    public void getSubItems(Item p_150895_1_, CreativeTabs p_150895_2_, List p_150895_3_){
    }
}
