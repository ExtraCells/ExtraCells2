package extracells.container;

import extracells.ItemEnum;
import extracells.container.slot.SlotFake;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.ItemFluidContainer;

public class ContainerInterfaceFluid extends ECContainer
{
	IInventory tileentity;

	public ContainerInterfaceFluid(IInventory inventoryPlayer, IInventory inventoryTileEntity)
	{
		super(inventoryTileEntity.getSizeInventory());

		tileentity = inventoryTileEntity;

		for (int i = 0; i < 6; i++)
		{
			addSlotToContainer(new SlotFake(inventoryTileEntity, i, i * 20 + 30, 84)
			{
				public boolean isItemValid(ItemStack itemstack)
				{
					return tileentity.isItemValidForSlot(0, itemstack);
				}

				public void putStack(ItemStack itemstack)
				{
					if (itemstack != null)
					{
						if (FluidContainerRegistry.isFilledContainer(itemstack))
						{
							FluidStack inContainer = FluidContainerRegistry.getFluidForFilledItem(itemstack);
							if (inContainer != null)
								itemstack = new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, inContainer.fluidID);
						} else if (itemstack.getItem() instanceof ItemFluidContainer)
						{
							FluidStack inContainer = ((ItemFluidContainer) itemstack.getItem()).getFluid(itemstack);
							if (inContainer != null)
								itemstack = new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, inContainer.fluidID);
						} else
						{
							itemstack = null;
						}

						if (itemstack != null)
						{
							this.inventory.setInventorySlotContents(getSlotIndex(), itemstack);
							this.onSlotChanged();
						}
					} else
					{
						this.inventory.setInventorySlotContents(getSlotIndex(), null);
						this.onSlotChanged();
					}
				}
			});
		}

		bindPlayerInventory(inventoryPlayer);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, i * 18 + 115));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 173));
		}
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
			itemstack.stackSize = 1;
			if (tileentity.isItemValidForSlot(slotnumber, itemstack1))
			{
				if (slotnumber >= 0 && slotnumber <= 5)
				{
					((Slot) inventorySlots.get(slotnumber)).putStack(null);
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer)
	{
		return true;
	}

	public ItemStack toFluidStack(ItemStack source)
	{
		if (FluidContainerRegistry.isFilledContainer(source))
		{
			FluidStack inContainer = FluidContainerRegistry.getFluidForFilledItem(source);
			if (inContainer != null)
				return new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, inContainer.fluidID);
		} else if (source.getItem() instanceof ItemFluidContainer)
		{
			FluidStack inContainer = ((ItemFluidContainer) source.getItem()).getFluid(source);
			if (inContainer != null)
				return new ItemStack(ItemEnum.FLUIDDISPLAY.getItemEntry(), 1, inContainer.fluidID);
		}
		return null;
	}

}