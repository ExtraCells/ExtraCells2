package extracells.items;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import appeng.api.IAEItemStack;
import appeng.api.IItemList;
import appeng.api.Materials;
import appeng.api.Util;
import appeng.api.config.FuzzyMode;
import appeng.api.me.items.IStorageCell;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;

public class ItemStorageFluid extends Item
{

	// Localization suffixes
	public static final String[] suffixes = new String[]
	{ "1k", "4k", "16k", "64k", "256k", "1m", "4m", "16m" };

	public static final int[] spaces = new int[]
	{ 1024, 4096, 16348, 65536, 262144, 1048576, 4194304, 16740352 };

	// Icons
	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemStorageFluid(int id)
	{
		super(id);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int par1)
	{
		int j = MathHelper.clamp_int(par1, 0, 7);
		return this.icons[j];
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		this.icons = new Icon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			this.icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i]);
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	public void getSubItems(int i, CreativeTabs creativeTab, List listSubItems)
	{
		for (int j = 0; j < suffixes.length; ++j)
		{
			listSubItems.add(new ItemStack(i, 1, j));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		return "item.storage.fluid." + suffixes[i];
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack)
	{
		Boolean hasName = !Util.getCellRegistry().getHandlerForCell(itemstack).getName().isEmpty();
		String partitionName = Util.getCellRegistry().getHandlerForCell(itemstack).getName();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(itemstack).usedBytes();
		if (itemstack.getItemDamage() == 4)
		{
			if (used_bytes != 0 && !Util.getCellRegistry().getHandlerForCell(itemstack).getAvailableItems().getItems().isEmpty())
			{
				return this.getLocalizedName(itemstack) + " - " + Util.getCellRegistry().getHandlerForCell(itemstack).getAvailableItems().getItems().get(0).getDisplayName();
			} else
			{
				return StatCollector.translateToLocal("tooltip.empty") + " " + this.getLocalizedName(itemstack);
			}
		} else
		{

			if (hasName)
			{
				return this.getLocalizedName(itemstack) + " - " + partitionName;
			} else
			{
				return this.getLocalizedName(itemstack);
			}
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		Boolean partitioned = Util.getCellRegistry().getHandlerForCell(stack).isPreformatted();
		Boolean fuzzy = Util.getCellRegistry().getHandlerForCell(stack).isFuzzyPreformatted();
		long used_bytes = Util.getCellRegistry().getHandlerForCell(stack).usedBytes();
		long total_bytes = Util.getCellRegistry().getHandlerForCell(stack).totalBytes();
		long used_types = Util.getCellRegistry().getHandlerForCell(stack).storedItemTypes();
		long total_types = Util.getCellRegistry().getHandlerForCell(stack).getTotalItemTypes();

		Util.getCellRegistry().getHandlerForCell(stack).getPreformattedItems();

		list.add(used_bytes / (FluidContainerRegistry.BUCKET_VOLUME * 8) + " of " + total_bytes / (FluidContainerRegistry.BUCKET_VOLUME * 8) + " bytes used");
		list.add(used_types + " of " + total_types + " fluid types used");
		if (used_bytes != 0)
			list.add("contains " + used_bytes + "mB of fluid");

		if (partitioned)
		{
			list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
		}
	}

	public int getBytes(ItemStack itemstack)
	{
		// 1 bytes equals 8 buckets, because 1 byte equals 8 items in normal storages
		return spaces[itemstack.getItemDamage()] * FluidContainerRegistry.BUCKET_VOLUME * 8;
	}

	public int getTotalTypes(ItemStack i)
	{
		return 5;
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}
}
