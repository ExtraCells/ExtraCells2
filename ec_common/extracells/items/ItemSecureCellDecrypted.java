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
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.me.items.IStorageCell;

public class ItemSecureCellDecrypted extends Item implements IStorageCell
{

	private Icon icon;

	public ItemSecureCellDecrypted(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
		this.setCreativeTab(extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int dmg)
	{
		return this.icon;
	}

	@Override
	public void registerIcons(IconRegister IconRegister)
	{
		this.icon = IconRegister.registerIcon("extracells:itemLockOpened");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "itemSecureCellDecrypted";
	}

	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		Boolean hasName = !Util.getCellRegistry().getHandlerForCell(stack).getName().isEmpty();
		String partitionName = Util.getCellRegistry().getHandlerForCell(stack).getName();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		if (hasName)
		{
			return "Encryptable Cell - Decrypted" + " - " + partitionName;
		} else
		{
			return "Encryptable Cell - Decrypted";
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		Boolean preformatted = Util.getCellRegistry().getHandlerForCell(stack).isPreformatted();
		Boolean fuzzy = Util.getCellRegistry().getHandlerForCell(stack).isFuzzyPreformatted();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		long total_bytes = Util.getCellRegistry().getHandlerForCell(stack).totalBytes();
		long used_types = Util.getCellRegistry().getHandlerForCell(stack).storedItemTypes();
		long total_types = Util.getCellRegistry().getHandlerForCell(stack).getTotalItemTypes();
		list.add(used_bytes + " of " + total_bytes + " Bytes Used");
		list.add(used_types + " of " + total_types + " Types");
		if (preformatted)
		{
			if (fuzzy)
			{
				list.add("Preformatted - Fuzzy");
			} else
			{
				list.add("Preformatted - Precise");
			}
		}
	}

	@ForgeSubscribe
	@Override
	public ItemStack onItemRightClick(ItemStack i, World w, EntityPlayer p)
	{
		if (p.isSneaking())
		{
			ItemStack itemStackDecrypted = p.inventory.getCurrentItem();
			ItemStack itemStackEncrypted = new ItemStack(extracells.CellEncrypted, 1);// p.inventory.getCurrentItem();
			if (!itemStackEncrypted.hasTagCompound())
			{
				itemStackEncrypted.setTagCompound(new NBTTagCompound());
			}

			if (!itemStackDecrypted.hasTagCompound())
			{
				itemStackDecrypted.setTagCompound(new NBTTagCompound());
			}

			NBTTagCompound tagEncrypted = itemStackEncrypted.getTagCompound();
			NBTTagCompound tagDecrypted = itemStackDecrypted.getTagCompound();

			//copy over content
			itemStackEncrypted.setTagCompound(tagDecrypted);
			
			//set owner
			itemStackEncrypted.getTagCompound().setString("owner", p.username);

			p.inventory.setInventorySlotContents(p.inventory.currentItem, itemStackEncrypted);
			if (!w.isRemote)
			{
				p.addChatMessage("Storage encrypted!");
			}
		}
		return i;
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return 1024;
	}

	@Override
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 63;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requsetedAddition)
	{
		return false;
	}

	@Override
	public boolean storableInStorageCell()
	{
		return false;
	}

}
