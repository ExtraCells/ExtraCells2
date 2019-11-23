package extracells.container;

import extracells.part.PartFluidLevelEmitter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ContainerFluidEmitter extends Container {

	PartFluidLevelEmitter part;
	EntityPlayer player;

	public ContainerFluidEmitter(PartFluidLevelEmitter _part,
			EntityPlayer _player) {
		super();
		this.part = _part;
		this.player = _player;
		bindPlayerInventory(this.player.inventory);
	}

	protected void bindPlayerInventory(IInventory inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9,
						8 + j * 18, i * 18 + 84));
			}
		}

		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer entityplayer) {
		return this.part.isValid();
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotnumber) {
		ItemStack itemstack = null;
		Slot slot = (Slot) this.inventorySlots.get(slotnumber);
		if (slot != null && slot.getHasStack()) {
			ItemStack fluidItem = slot.getStack().copy();
			fluidItem.stackSize = 1;
			FluidStack fluidStack = FluidContainerRegistry
					.getFluidForFilledItem(fluidItem);
			if (fluidStack == null) {
				return null;
			}
			this.part.setFluid(0, fluidStack.getFluid(), player);
			return null;
		}
		return null;
	}
}
