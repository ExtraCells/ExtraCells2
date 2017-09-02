package extracells.item.storage;

import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import appeng.api.config.FuzzyMode;
import appeng.api.storage.StorageChannel;
import extracells.api.IFluidStorageCell;
import extracells.inventory.ECFluidFilterInventory;
import extracells.inventory.ECPrivateInventory;
import extracells.item.ItemFluid;

public class ItemStorageCellFluid extends ItemStorageCell implements IFluidStorageCell {

	public ItemStorageCellFluid() {
		super(CellDefinition.FLUID, StorageChannel.FLUIDS);
	}

	@Override
	public IInventory getConfigInventory(ItemStack is) {
		return new ECFluidFilterInventory("configFluidCell", 63, is);
	}

	@Override
	public ArrayList<Fluid> getFilter(ItemStack stack) {
		ECFluidFilterInventory inventory = new ECFluidFilterInventory("", 63, stack);
		ItemStack[] itemStacks = inventory.slots;
		ArrayList<Fluid> filter = new ArrayList<Fluid>();
		if (itemStacks.length == 0) {
			return null;
		}
		for (ItemStack itemStack : itemStacks) {
			if (itemStack == null) {
				continue;
			}
			String fluidName = ItemFluid.getFluidName(itemStack);
			Fluid fluid = FluidRegistry.getFluid(fluidName);
			if (fluid != null) {
				filter.add(fluid);
			}
		}
		return filter;
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
	public IInventory getUpgradesInventory(ItemStack is) {
		return new ECPrivateInventory("configInventory", 0, 64);
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		if (is == null)
			return;
		NBTTagCompound tag;
		if (is.hasTagCompound())
			tag = is.getTagCompound();
		else
			tag = new NBTTagCompound();
		tag.setString("fuzzyMode", fzMode.name());
		is.setTagCompound(tag);

	}
}
