package extracells.container.slot;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotFake extends Slot 
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