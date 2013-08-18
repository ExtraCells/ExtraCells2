package extracells.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;

public class SlotFake extends Slot
{
	public SlotFake(IInventory inv, int index, int x, int y)
	{
		super(inv, index, x, y);
	}

	@Override
	public boolean isItemValid(ItemStack stack)
	{
		return true;
	}

	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		return false;
	}
}