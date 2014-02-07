package extracells.container;

import extracells.gui.GuiBusIOFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import extracells.part.PartFluidIO;

public class ContainerBusIOFluid extends Container
{
	private PartFluidIO terminal;
	private EntityPlayer player;
	private GuiBusIOFluid guiBusIOFluid;

	public ContainerBusIOFluid(PartFluidIO _terminal, EntityPlayer _player)
	{
		terminal = _terminal;
		player = _player;
		bindPlayerInventory(player.inventory);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 79));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 137));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber)
	{
		if (guiBusIOFluid != null)
			guiBusIOFluid.shiftClick(getSlot(slotnumber).getStack());
		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	public void setGui(GuiBusIOFluid _guiBusIOFluid)
	{
		guiBusIOFluid = _guiBusIOFluid;
	}
}
