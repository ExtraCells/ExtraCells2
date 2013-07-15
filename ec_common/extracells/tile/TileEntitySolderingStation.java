package extracells.tile;

import java.util.ArrayList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntitySolderingStation extends TileEntity
{
	private ArrayList<String> users;

	public TileEntitySolderingStation()
	{
		users = new ArrayList<String>();
	}

	public void addUser(String name)
	{
		if (!users.contains(name))
		{
			users.add(name);
		}
	}

	public void remUser(String name)
	{
		users.remove(name);
	}

	public void updateData(String user, int size, int types, char upgrade, char downgrade)
	{
		EntityPlayer p = worldObj.getPlayerEntityByName(user);
		if (users.contains(user))
		{
			if (p.getHeldItem() != null)
			{
				ItemStack stack = p.getHeldItem();
				if (!stack.hasTagCompound())
					stack.setTagCompound(new NBTTagCompound());
				NBTTagCompound tag = stack.getTagCompound();
				tag.setInteger("costum_size", size);
				tag.setInteger("costum_types", types);
			}
		}

		if (upgrade != '\0')
		{
			switch (upgrade)
			{
			case 's':
				decreaseStackInInv(p, appeng.api.Materials.matStorageCell.copy(), 1);
				break;
			case 't':
				decreaseStackInInv(p, appeng.api.Materials.matConversionMatrix.copy(), 1);
				break;
			}
		}

		if (downgrade != '\0')
		{
			switch (downgrade)

			{
			case 's':
				p.inventory.addItemStackToInventory(new ItemStack(appeng.api.Materials.matStorageCell.copy().getItem(), 1, appeng.api.Materials.matStorageCell.getItemDamage()));
				break;
			case 't':
				p.inventory.addItemStackToInventory(new ItemStack(appeng.api.Materials.matConversionMatrix.copy().getItem(), 1, appeng.api.Materials.matConversionMatrix.getItemDamage()));
				break;
			}
		}
	}

	// Find ItemStack in inventory are remove int number items from it
	public void decreaseStackInInv(EntityPlayer user, ItemStack itemstack, int number)
	{
		int left = number;
		for (int i = 0; i < user.inventory.mainInventory.length; i++)
		{
			if (user.inventory.mainInventory[i] != null)
			{
				if (user.inventory.mainInventory[i].getItem() == itemstack.getItem() && user.inventory.mainInventory[i].getItemDamage() == itemstack.getItemDamage())
				{
					if (left != 0)
					{
						if (user.inventory.mainInventory[i].stackSize == 1)
						{
							user.inventory.mainInventory[i] = null;
							left = left - 1;
						} else
						{
							user.inventory.mainInventory[i] = new ItemStack(user.inventory.mainInventory[i].getItem(), user.inventory.mainInventory[i].stackSize - 1, user.inventory.mainInventory[i].getItemDamage());
							left = left - 1;
						}
					}
				}
			}
		}
	}
}