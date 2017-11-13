package extracells.inventory;

import extracells.integration.Integration;
import extracells.item.ItemGas;
import extracells.registries.ItemEnum;
import extracells.util.GasUtil;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import mekanism.api.gas.GasStack;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.Optional;

public class ECGasFilterInventory extends InventoryPlain {

	private final ItemStack cellItem;

	public ECGasFilterInventory(String customName, int size, ItemStack cellItem) {
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
		if(Integration.Mods.MEKANISMGAS.isEnabled())
			return isItemValidForSlotGas(index, itemStack);
		return false;
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public boolean isItemValidForSlotGas(int index, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			return false;
		}
		if (itemStack.getItem() == ItemEnum.GASITEM.getItem()) {
			String gasName = ItemGas.getGasName(itemStack);
			for (ItemStack slotStack : this.slots) {
				if (slotStack == null || slotStack.isEmpty()) {
					continue;
				}
				String itemGasName = ItemGas.getGasName(slotStack);
				if (itemGasName.equals(gasName)) {
					return false;
				}
			}
			return true;
		}
		GasStack stack = GasUtil.getGasFromContainer(itemStack);
		if (stack == null) {
			return false;
		}
		String gasName = stack.getGas().getName();
		for (ItemStack slotStack : this.slots) {
			if (slotStack == null || slotStack.isEmpty()) {
				continue;
			}
			String itemGasName = ItemGas.getGasName(slotStack);
			if (itemGasName.equals(gasName)) {
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
		if(Integration.Mods.MEKANISMGAS.isEnabled())
			setInventorySlotContentsGas(index, itemStack);
		else
			super.setInventorySlotContents(index, ItemStack.EMPTY);
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public void setInventorySlotContentsGas(int index, ItemStack itemStack) {
		if (itemStack == null || itemStack.isEmpty()) {
			super.setInventorySlotContents(index, ItemStack.EMPTY);
			return;
		}
		Gas gas;
		if (itemStack.getItem() == ItemEnum.GASITEM.getItem()) {
			gas = GasRegistry.getGas(ItemGas.getGasName(itemStack));
			if (gas == null) {
				return;
			}
		} else {
			if (!isItemValidForSlot(index, itemStack)) {
				return;
			}
			GasStack gasStack = GasUtil.getGasFromContainer(itemStack);
			if (gasStack == null) {
				super.setInventorySlotContents(index, ItemStack.EMPTY);
				return;
			}
			gas = gasStack.getGas();
			if (gas == null) {
				super.setInventorySlotContents(index, ItemStack.EMPTY);
				return;
			}
		}
		ItemStack stack = new ItemStack(ItemEnum.GASITEM.getItem());
		ItemGas.setGasName(stack, gas.getName());
		super.setInventorySlotContents(index, stack);
	}

}
