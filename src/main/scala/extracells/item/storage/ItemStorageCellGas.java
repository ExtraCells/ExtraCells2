package extracells.item.storage;

import java.util.ArrayList;

import extracells.inventory.ECGasFilterInventory;
import extracells.item.ItemGas;
import extracells.util.StorageChannels;
import mekanism.api.gas.Gas;
import mekanism.api.gas.GasRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import appeng.api.config.FuzzyMode;
import extracells.api.IGasStorageCell;
import extracells.inventory.ECFluidFilterInventory;
import extracells.inventory.InventoryPlain;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;

public class ItemStorageCellGas extends ItemStorageCell implements IGasStorageCell {

	public ItemStorageCellGas() {
		super(CellDefinition.GAS, StorageChannels.GAS());
	}

	@Override
	public IItemHandler getConfigInventory(ItemStack is) {
		return new InvWrapper(new ECGasFilterInventory("configFluidCell", 63, is));
	}

	@Override
	public ArrayList<Object> getFilter(ItemStack stack) {
		if (channel != null)
			return getFilterGas(stack);
		return new ArrayList<Object>();
	}

	@Optional.Method(modid = "MekanismAPI|gas")
	public ArrayList<Object> getFilterGas(ItemStack stack){
		ECGasFilterInventory inventory = new ECGasFilterInventory("", 63, stack);
		ItemStack[] itemStacks = inventory.slots;
		ArrayList<Gas> filter = new ArrayList<Gas>();
		if (itemStacks.length == 0) {
			return null;
		}
		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}
			String gasName = ItemGas.getGasName(itemStack);
			Gas gas = GasRegistry.getGas(gasName);
			if (gas != null) {
				filter.add(gas);
			}
		}
		return (ArrayList<Object>)(Object) filter;
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is) {
		if (is == null) {
			return null;
		}
		if (!is.hasTagCompound()) {
			is.setTagCompound(new NBTTagCompound());
		}
		if (is.getTagCompound().hasKey("fuzzyMode")) {
			return FuzzyMode.valueOf(is.getTagCompound().getString("fuzzyMode"));
		}
		is.getTagCompound().setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name());
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public int getMaxBytes(ItemStack itemStack) {
		StorageType type = definition.cells.fromMeta(itemStack.getItemDamage());
		return type.getBytes();
	}

	@Override
	public int getMaxTypes(ItemStack unused) {
		return 5;
	}

	@Override
	public IItemHandler getUpgradesInventory(ItemStack is) {
		return new InvWrapper(new InventoryPlain("configInventory", 0, 64));
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		if (is == null) {
			return;
		}
		NBTTagCompound tag;
		if (is.hasTagCompound()) {
			tag = is.getTagCompound();
		} else {
			tag = new NBTTagCompound();
		}
		tag.setString("fuzzyMode", fzMode.name());
		is.setTagCompound(tag);

	}
}
