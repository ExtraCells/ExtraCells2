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
import extracells.inventoryHandler.HandlerItemStorageFluid;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemStorageFluid extends Item implements ICellHandler
{
	public static final String[] suffixes =
	{ "1k", "4k", "16k", "64k", "256k", "1024k", "4096k", "16348k" };

	public static final int[] spaces =
	{ 1024, 4096, 16348, 65536, 262144, 1048576, 4194304, 16777216 };

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	public ItemStorageFluid()
	{
		AEApi.instance().registries().cell().addCellHandler(this);
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int dmg)
	{
		int j = MathHelper.clamp_int(dmg, 0, suffixes.length);
		return icons[j];
	}

	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		icons = new IIcon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i)
		{
			icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i]);
		}
	}

	@SuppressWarnings(
	{ "rawtypes", "unchecked" })
	@SideOnly(Side.CLIENT)
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab, List listSubItems)
	{
		for (int i = 0; i < suffixes.length; ++i)
		{
			listSubItems.add(new ItemStack(item, 1, i));
		}
	}

	public String getUnlocalizedName(ItemStack itemStack)
	{
		return "item.storage.fluid." + suffixes[itemStack.getItemDamage()];
	}

	@Override
	public boolean isCell(ItemStack is)
	{
		return is.getItem() == this;
	}

	@Override
	public IMEInventoryHandler getCellInventory(ItemStack is, StorageChannel channel)
	{
		if (channel == StorageChannel.ITEMS || is.getItem() != this)
			return null;
		return new HandlerItemStorageFluid(is);
	}

	@Override
	public IIcon getTopTexture()
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
}
