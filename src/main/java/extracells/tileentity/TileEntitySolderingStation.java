package extracells.tileentity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import appeng.api.Materials;
import extracells.ItemEnum;

public class TileEntitySolderingStation extends TileEntity
{
	public boolean canUpdate()
	{
		return false;
	}

	public void changeTypes(EntityPlayer player, int slotID, int typesDelta)
	{
		ItemStack storage = player.inventory.getCurrentItem().copy();
		if (storage != null && storage.getItem() == ItemEnum.STORAGEPHYSICAL.getItemInstance() && storage.getItemDamage() == 5)
		{

			if (!storage.hasTagCompound())
			{
				storage.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = storage.getTagCompound();
			int oldTypes = nbt.getInteger("custom_types");
			if (oldTypes + typesDelta >= 27 && oldTypes + typesDelta <= 63 && typesDelta != 0)
			{
				if (typesDelta > 0)
				{
					if (decreaseItemStack(Materials.matConversionMatrix.copy(), player.inventory))
					{
						nbt.setInteger("custom_types", oldTypes + typesDelta);
						player.inventory.mainInventory[player.inventory.currentItem] = storage;
					}
				} else if (typesDelta < 0)
				{
					if (player.inventory.addItemStackToInventory(Materials.matConversionMatrix.copy()))
					{
						nbt.setInteger("custom_types", oldTypes + typesDelta);
						player.inventory.mainInventory[player.inventory.currentItem] = storage;
					}
				}
			}
		}
	}

	public void changeStorage(EntityPlayer player, int slotID, int storageDelta)
	{
		ItemStack storage = player.inventory.getCurrentItem().copy();
		if (storage != null && storage.getItem() == ItemEnum.STORAGEPHYSICAL.getItemInstance() && storage.getItemDamage() == 5)
		{
			if (!storage.hasTagCompound())
			{
				storage.setTagCompound(new NBTTagCompound());
			}
			NBTTagCompound nbt = storage.getTagCompound();
			int oldSize = nbt.getInteger("custom_size");
			if (oldSize + storageDelta >= 4096 && storageDelta != 0)
			{
				if (storageDelta > 0)
				{
					if (decreaseItemStack(Materials.matStorageCell.copy(), player.inventory))
						nbt.setInteger("custom_size", oldSize + storageDelta);
					player.inventory.mainInventory[player.inventory.currentItem] = storage;
				} else if (storageDelta < 0)
				{
					if (player.inventory.addItemStackToInventory(Materials.matStorageCell.copy()))
						nbt.setInteger("custom_size", oldSize + storageDelta);
					player.inventory.mainInventory[player.inventory.currentItem] = storage;
				}
			}
		}
	}

	public Boolean decreaseItemStack(ItemStack toRemove, IInventory inventory)
	{
		for (int i = 0; i < inventory.getSizeInventory(); i++)
		{
			ItemStack current = inventory.getStackInSlot(i);
			if (current != null && current.isItemEqual(toRemove))
			{
				inventory.decrStackSize(i, 1);
				return true;
			}
		}
		return false;
	}
}
