package extracells.inventory.cell;


import java.util.ArrayList;

import mekanism.api.gas.Gas;
import mekanism.api.gas.GasStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;

import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import appeng.api.storage.ISaveProvider;

public class HandlerItemPlayerStorageGas extends HandlerItemStorageGas {

	private final EntityPlayer player;
	private final EnumHand hand;

	public HandlerItemPlayerStorageGas(ItemStack _storageStack, ISaveProvider _saveProvider, ArrayList<Gas> _filter, EntityPlayer _player, EnumHand hand) {
		super(_storageStack, _saveProvider, _filter);
		this.player = _player;
		this.hand = hand;
	}

	public HandlerItemPlayerStorageGas(ItemStack _storageStack,
		ISaveProvider _saveProvider, EntityPlayer _player, EnumHand hand) {
		super(_storageStack, _saveProvider);
		this.player = _player;
		this.hand = hand;
	}

	@Override
	protected void writeGasToSlot(int i, GasStack gasStack) {
		ItemStack item = player.getHeldItem(hand);
		if (item == null) {
			return;
		}
		if (!item.hasTagCompound()) {
			item.setTagCompound(new NBTTagCompound());
		}
		NBTTagCompound gasTag = new NBTTagCompound();
		if (gasStack != null && gasStack.amount > 0) {
			gasTag = gasStack.write(gasTag);
			item.getTagCompound().setTag("Gas#" + i, gasTag);
		} else {
			item.getTagCompound().removeTag("Gas#" + i);
		}
		this.gasStacks.set(i, gasStack);
		//Remove old Fluid Tag
		item.getTagCompound().removeTag("Fluid#" + i);
	}
}
