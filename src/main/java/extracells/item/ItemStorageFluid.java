package extracells.item;

import appeng.api.AEApi;
import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.ItemEnum;
import extracells.inventoryHandler.HandlerItemStorageFluid;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemStorageFluid extends Item implements ICellHandler
{
	public static final String[] suffixes =
	{ "1k", "4k", "16k", "64k" };

	public static final int[] spaces =
	{ 1024, 4096, 16348, 65536 };

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemStorageFluid(int itemId)
	{
		super(itemId);
		AEApi.instance().registries().cell().addCellHandler(this);
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
		setCreativeTab(Extracells.ModTab);
	}

	@SideOnly(Side.CLIENT)
	public Icon getIconFromDamage(int dmg)
	{
		int j = MathHelper.clamp_int(dmg, 0, suffixes.length);
		return icons[j];
	}

	@Override
	public void registerIcons(IconRegister iconRegister)
	{
		icons = new Icon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i]);
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemId, CreativeTabs creativeTab, List listSubItems)
	{
		for (int i = 0; i < suffixes.length; ++i)
		{
			listSubItems.add(new ItemStack(itemId, 1, i));
		}
	}

	@Override
	public boolean isCell(ItemStack is)
	{
		return true;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack is, StorageChannel channel)
	{
		if (channel == StorageChannel.ITEMS)
			return null;
		return new HandlerItemStorageFluid(is);
	}

	@Override
	public Icon getTopTexture()
	{
		return null; // TODO
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan)
	{
		// TODO
	}

	@Override
	public int getStatusForCell(ItemStack is, IMEInventory handler)
	{
		return 0;
	}

	@Override
	public double cellIdleDrain(ItemStack is, IMEInventory handler)
	{
		return 0;
	}

	public int maxTypes(ItemStack is)
	{
		return 5;
	}

	public int maxStorage(ItemStack is)
	{
		return spaces[Math.max(0, is.getItemDamage())];
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4)
	{
		IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(stack, StorageChannel.FLUIDS);
		if (!(handler instanceof HandlerItemStorageFluid))
			return;
		HandlerItemStorageFluid cellHandler = (HandlerItemStorageFluid) handler;
		Boolean partitioned = cellHandler.isPreformatted();
		long used_bytes = cellHandler.usedBytes();
		long total_bytes = cellHandler.totalBytes();
		long used_types = cellHandler.usedTypes();
		long total_types = cellHandler.totalTypes();

		list.add((used_bytes / 250) + " of " + total_bytes / 250 + " bytes used");
		list.add(used_types + " of " + total_types + " fluid types used");
		if (used_bytes != 0)
			list.add("contains " + used_bytes + "mB of fluid");

		if (partitioned)
		{
			list.add(StatCollector.translateToLocal("Appeng.GuiITooltip.Partitioned") + " - " + StatCollector.translateToLocal("Appeng.GuiITooltip.Precise"));
		}
	}

	public EnumRarity getRarity(ItemStack par1)
	{
		return EnumRarity.epic;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		return ItemEnum.FLUIDSTORAGE.getInternalName() + "." + suffixes[i];
	}
}
