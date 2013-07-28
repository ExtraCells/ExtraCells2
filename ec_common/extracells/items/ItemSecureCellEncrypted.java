package extracells.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.extracells;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;

public class ItemSecureCellEncrypted extends Item
{

	private Icon icon;

	public ItemSecureCellEncrypted(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int dmg)
	{
		return this.icon;
	}

	@Override
	public void registerIcons(IconRegister IconRegister)
	{
		this.icon = IconRegister.registerIcon("extracells:itemMEEncryptableStorageEncrypted");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "itemSecureCellEncrypted";
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		if (!stack.hasTagCompound())
		{
			stack.setTagCompound(new NBTTagCompound());
		}

		list.add("This Storage belongs to " + stack.getTagCompound().getString("owner") + "!");
	}

	@ForgeSubscribe
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World w, EntityPlayer p)
	{
		ItemStack itemStackEncrypted = p.inventory.getCurrentItem();
		ItemStack itemStackDecrypted = new ItemStack(extracells.CellDecrypted, 1);

		if (!itemStackEncrypted.hasTagCompound())
		{
			itemStackEncrypted.setTagCompound(new NBTTagCompound());
		}

		// copy over content
		NBTTagCompound tagEncrypted = itemStackEncrypted.getTagCompound();

		// remove owner
		String user = tagEncrypted.getString("owner");

		if (p.isSneaking())
		{
			if (p.username.equals(user))
			{
				tagEncrypted.removeTag("owner");
				itemStackDecrypted.setTagCompound((tagEncrypted));

				p.inventory.setInventorySlotContents(p.inventory.currentItem, itemStackDecrypted);

				if (!w.isRemote)
				{
					p.addChatMessage("Access granted!");
				}
			} else
			{
				if (!w.isRemote)
				{
					p.addChatMessage("You are not the owner of this storage drive!!!");
				}
			}
		}
		return stack;
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}
}
