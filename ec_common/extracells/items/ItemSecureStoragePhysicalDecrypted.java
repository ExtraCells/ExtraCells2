package extracells.items;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeSubscribe;
import appeng.api.IAEItemStack;
import appeng.api.Util;
import appeng.api.me.items.IStorageCell;

public class ItemSecureStoragePhysicalDecrypted extends Item implements IStorageCell
{

	private Icon icon;

	public ItemSecureStoragePhysicalDecrypted(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(false);
		this.setCreativeTab(Extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int dmg)
	{
		return this.icon;
	}

	@Override
	public void registerIcons(IconRegister IconRegister)
	{
		this.icon = IconRegister.registerIcon("extracells:storage.physical_secure.decrypted");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack)
	{
		return "item.storage.physical_secure.decrypted";
	}

	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		Boolean hasName = !Util.getCellRegistry().getHandlerForCell(stack).getName().isEmpty();
		String partitionName = Util.getCellRegistry().getHandlerForCell(stack).getName();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		if (hasName)
		{
			return StatCollector.translateToLocal(getUnlocalizedName(stack)) + " - " + partitionName;
		} else
		{
			return StatCollector.translateToLocal(getUnlocalizedName(stack));
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4)
	{
		Boolean partitioned = Util.getCellRegistry().getHandlerForCell(itemstack).isPreformatted();
		Boolean fuzzy = Util.getCellRegistry().getHandlerForCell(itemstack).isFuzzyPreformatted();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(itemstack).usedBytes();
		long total_bytes = Util.getCellRegistry().getHandlerForCell(itemstack).totalBytes();
		long used_types = Util.getCellRegistry().getHandlerForCell(itemstack).storedItemTypes();
		long total_types = Util.getCellRegistry().getHandlerForCell(itemstack).getTotalItemTypes();
		list.add(used_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Of") + " " + total_bytes + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.BytesUsed"));
		list.add(used_types + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Of") + " " + total_types + " " + StatCollector.translateToLocal("Appeng.GuiITooltip.Types"));
		if (partitioned)
		{
			if (fuzzy)
			{
				list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Fuzzy"));
			} else
			{
				list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
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
			ItemStack itemStackEncrypted = new ItemStack(Extracells.StoragePhysicalEncrypted, 1);
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

			// copy over content
			itemStackEncrypted.setTagCompound(tagDecrypted);

			// set owner
			itemStackEncrypted.getTagCompound().setString("owner", p.username);

			p.inventory.setInventorySlotContents(p.inventory.currentItem, itemStackEncrypted);
			if (!w.isRemote)
			{
				p.addChatMessage(StatCollector.translateToLocal("tooltip.storageencrypted"));
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
