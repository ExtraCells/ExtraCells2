package extracells.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotFake extends Slot implements IPhantomSlot
{
	public SlotFake(IInventory inv, int index, int x, int y)
	{
		super(inv, index, x, y);
	}

	@Override
	public boolean canTakeStack(EntityPlayer player)
	{
		return false;
	}

	@Override
	public boolean canAdjust()
	{
		return true;
	}

	@Override
	public int getSlotStackLimit()
	{
		return 1;
	}
}