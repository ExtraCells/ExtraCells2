package extracells.item;

import appeng.api.implementations.tiles.IChestOrDrive;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.inventoryHandler.HandlerItemStorageFluid;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;

import java.util.List;

public class ItemStorageFluid extends Item implements ICellHandler
{
	public static final String[] suffixes =
			{"1k", "4k", "16k", "64k"};

	public static final int[] spaces =
			{1024, 4096, 16348, 65536};

	@SideOnly(Side.CLIENT)
	private Icon[] icons;

	public ItemStorageFluid(int itemId)
	{
		super(itemId);
		this.setMaxStackSize(1);
		this.setMaxDamage(0);
		this.setHasSubtypes(true);
		this.setCreativeTab(Extracells.ModTab);
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
			this.icons[i] = iconRegister.registerIcon("extracells:" + "storage.fluid." + suffixes[i]);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@SideOnly(Side.CLIENT)
	public void getSubItems(int itemId, CreativeTabs creativeTab, List listSubItems)
	{
		for (int i = 0; i < suffixes.length; ++i)
		{
			listSubItems.add(new ItemStack(i, 1, i));
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
		return null; //TODO
	}

	@Override
	public void openChestGui(EntityPlayer player, IChestOrDrive chest, ICellHandler cellHandler, IMEInventoryHandler inv, ItemStack is, StorageChannel chan)
	{
		//TODO
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
}
