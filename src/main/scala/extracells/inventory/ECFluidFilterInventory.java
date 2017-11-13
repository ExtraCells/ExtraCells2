package extracells.inventory;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import extracells.item.ItemFluid;
import extracells.registries.ItemEnum;

public class ECFluidFilterInventory extends InventoryPlain {

	private final ItemStack cellItem;

	public ECFluidFilterInventory(String customName, int size, ItemStack cellItem) {
		super(customName, size, 1);
		this.cellItem = cellItem;
		if (this.cellItem.hasTagCompound()) {
			if (this.cellItem.getTagCompound().hasKey("filter")) {
				readFromNBT(this.cellItem.getTagCompound().getTagList("filter", 10));
			}
		}
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return false;
		}
		if (itemStack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
			String fluidName = ItemFluid.getFluidName(itemStack);
			for (ItemStack slotStack : this.slots) {
				if (slotStack == null || slotStack.isEmpty()) {
					continue;
				}
				String itemFluidName = ItemFluid.getFluidName(slotStack);
				if (itemFluidName.equals(fluidName)) {
					return false;
				}
			}
			return true;
		}
		FluidStack stack = FluidUtil.getFluidContained(itemStack);
		if (stack == null) {
			return false;
		}
		String fluidName = stack.getFluid().getName();
		for (ItemStack slotStack : this.slots) {
			if (slotStack == null || slotStack.isEmpty()) {
				continue;
			}
			String itemFluidName = ItemFluid.getFluidName(slotStack);
			if (itemFluidName.equals(fluidName)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void markDirty() {
		NBTTagCompound tag;
		if (this.cellItem.hasTagCompound()) {
			tag = this.cellItem.getTagCompound();
		} else {
			tag = new NBTTagCompound();
		}
		tag.setTag("filter", writeToNBT());
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			super.setInventorySlotContents(index, ItemStack.EMPTY);
			return;
		}
		Fluid fluid;
		if (itemStack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
			fluid = FluidRegistry.getFluid(ItemFluid.getFluidName(itemStack));
			if (fluid == null) {
				return;
			}
		} else {
			if (!isItemValidForSlot(index, itemStack)) {
				return;
			}
			FluidStack fluidStack = FluidUtil.getFluidContained(itemStack);
			if (fluidStack == null) {
				super.setInventorySlotContents(index, ItemStack.EMPTY);
				return;
			}
			fluid = fluidStack.getFluid();
			if (fluid == null) {
				super.setInventorySlotContents(index, ItemStack.EMPTY);
				return;
			}
		}
		ItemStack stack = new ItemStack(ItemEnum.FLUIDITEM.getItem());
		ItemFluid.setFluidName(stack, fluid.getName());
		super.setInventorySlotContents(index, stack);
	}

}
