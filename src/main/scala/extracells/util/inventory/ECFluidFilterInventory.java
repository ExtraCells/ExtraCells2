package extracells.util.inventory;

import extracells.registries.ItemEnum;
import extracells.util.FluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ECFluidFilterInventory extends ECPrivateInventory {

	private final ItemStack cellItem;

	public ECFluidFilterInventory(String _customName, int _size,
			ItemStack _cellItem) {
		super(_customName, _size, 1);
		this.cellItem = _cellItem;
		if (this.cellItem.hasTagCompound())
			if (this.cellItem.getTagCompound().hasKey("filter"))
				readFromNBT(this.cellItem.getTagCompound().getTagList("filter",
						10));
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		if (itemstack == null)
			return false;
		if (itemstack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
			int fluidID = itemstack.getItemDamage();
			for (ItemStack s : this.slots) {
				if (s == null)
					continue;
				if (s.getItemDamage() == fluidID)
					return false;
			}
			return true;
		}
		if (!FluidUtil.isFilled(itemstack))
			return false;
		FluidStack stack = FluidUtil.getFluidFromContainer(itemstack);
		if (stack == null)
			return false;
		int fluidID = stack.getFluidID();
		for (ItemStack s : this.slots) {
			if (s == null)
				continue;
			if (s.getItemDamage() == fluidID)
				return false;
		}
		return true;
	}

	@Override
	public void markDirty() {
		NBTTagCompound tag;
		if (this.cellItem.hasTagCompound())
			tag = this.cellItem.getTagCompound();
		else
			tag = new NBTTagCompound();
		tag.setTag("filter", writeToNBT());
	}

	@Override
	public void setInventorySlotContents(int slotId, ItemStack itemstack) {
		if (itemstack == null) {
			super.setInventorySlotContents(slotId, null);
			return;
		}
		Fluid fluid;
		if (itemstack.getItem() == ItemEnum.FLUIDITEM.getItem()) {
			fluid = FluidRegistry.getFluid(itemstack.getItemDamage());
			if (fluid == null)
				return;
		} else {
			if (!isItemValidForSlot(slotId, itemstack))
				return;
			FluidStack fluidStack = FluidUtil.getFluidFromContainer(itemstack);
			if (fluidStack == null) {
				super.setInventorySlotContents(slotId, null);
				return;
			}
			fluid = fluidStack.getFluid();
			if (fluid == null) {
				super.setInventorySlotContents(slotId, null);
				return;
			}
		}
		super.setInventorySlotContents(slotId,
				new ItemStack(ItemEnum.FLUIDITEM.getItem(), 1, fluid.getID()));
	}

}
