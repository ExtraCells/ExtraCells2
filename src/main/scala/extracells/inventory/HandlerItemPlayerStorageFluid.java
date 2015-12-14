package extracells.inventory;

import appeng.api.storage.ISaveProvider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;

public class HandlerItemPlayerStorageFluid extends HandlerItemStorageFluid {

	private final EntityPlayer player;

	public HandlerItemPlayerStorageFluid(ItemStack _storageStack,
			ISaveProvider _saveProvider, ArrayList<Fluid> _filter,
			EntityPlayer _player) {
		super(_storageStack, _saveProvider, _filter);
		this.player = _player;
	}

	public HandlerItemPlayerStorageFluid(ItemStack _storageStack,
			ISaveProvider _saveProvider, EntityPlayer _player) {
		super(_storageStack, _saveProvider);
		this.player = _player;
	}

	@Override
	protected void writeFluidToSlot(int i, FluidStack fluidStack) {
		if (this.player.getCurrentEquippedItem() == null)
			return;
		ItemStack item = this.player.getCurrentEquippedItem();
		if (!item.hasTagCompound())
			item.setTagCompound(new NBTTagCompound());
		NBTTagCompound fluidTag = new NBTTagCompound();
		if (fluidStack != null && fluidStack.getFluidID() > 0
				&& fluidStack.amount > 0) {
			fluidStack.writeToNBT(fluidTag);
			item.getTagCompound().setTag("Fluid#" + i, fluidTag);
		} else {
			item.getTagCompound().removeTag("Fluid#" + i);
		}
		this.fluidStacks.set(i, fluidStack);
	}

}
