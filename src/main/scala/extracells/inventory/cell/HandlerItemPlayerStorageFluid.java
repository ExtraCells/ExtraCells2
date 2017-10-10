package extracells.inventory.cell;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.ISaveProvider;

public class HandlerItemPlayerStorageFluid extends HandlerItemStorageFluid {

	private final EntityPlayer player;
	private final EnumHand hand;

	public HandlerItemPlayerStorageFluid(ItemStack _storageStack,
		ISaveProvider _saveProvider, ArrayList<Fluid> _filter,
		EntityPlayer _player, EnumHand hand) {
		super(_storageStack, _saveProvider, _filter);
		this.player = _player;
		this.hand = hand;
	}

	public HandlerItemPlayerStorageFluid(ItemStack _storageStack,
		ISaveProvider _saveProvider, EntityPlayer _player, EnumHand hand) {
		super(_storageStack, _saveProvider);
		this.player = _player;
		this.hand = hand;
	}

	@Override
	protected void writeFluidToSlot(int i, FluidStack fluidStack) {
		ItemStack item = player.getHeldItem(hand);
		if (item == null) {
			return;
		}
		if (!item.hasTagCompound()) {
			item.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound fluidTag = new NBTTagCompound();
		if (fluidStack != null && fluidStack.amount > 0) {
			fluidStack.writeToNBT(fluidTag);
			item.getTagCompound().setTag("Fluid#" + i, fluidTag);
		} else {
			item.getTagCompound().removeTag("Fluid#" + i);
		}
		this.fluidStacks.set(i, fluidStack);
	}

}
