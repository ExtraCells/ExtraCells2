package extracells.container;

import java.util.ArrayList;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import extracells.slot.SlotRespective;
import extracells.tile.TileEntityTerminalFluid;

public class ContainerTerminalFluid extends Container
{

	IInventory tileentity;

	public ContainerTerminalFluid(InventoryPlayer inventory, TileEntity tileentity)
	{
		this.tileentity = (IInventory) tileentity;
		// Input Slot accepts all FluidContainers
		addSlotToContainer(new SlotRespective(this.tileentity, 0, 7, 66));
		// Input Slot accepts nothing
		addSlotToContainer(new SlotRespective(this.tileentity, 1, 36, 66)
		{
			public boolean isItemValid(ItemStack itemstack)
			{
				return false;
			}
		});
		// Preview Slot
		addSlotToContainer(new SlotRespective(this.tileentity, 2, 130, 16)
		{
			public boolean canTakeStack(EntityPlayer par1EntityPlayer)
			{
				return false;
			}
		});

		bindPlayerInventory(inventory);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber)
	{
		ItemStack itemstack = null;
		Slot slot = (Slot) inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack())
		{
			ItemStack itemstack1 = slot.getStack();
			itemstack = itemstack1.copy();
			if (FluidContainerRegistry.isContainer(itemstack))
			{
				if (slotnumber == 1 || slotnumber == 0)
				{
					if (!mergeItemStack(itemstack1, 2, 38, false))
						return null;
				} else if (slotnumber != 1 && slotnumber != 0)
				{
					if (!mergeItemStack(itemstack1, 0, 1, false))
						return null;
				}
				if (itemstack1.stackSize == 0)
				{
					slot.putStack(null);
				} else
				{
					slot.onSlotChanged();
				}
			} else
			{
				return null;
			}
		}
		return itemstack;
	}

	@Override
	public void onContainerClosed(EntityPlayer entityplayer)
	{
		super.onContainerClosed(entityplayer);
	}
}
