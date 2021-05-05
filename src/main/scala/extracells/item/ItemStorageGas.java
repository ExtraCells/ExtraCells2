package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.FuzzyMode;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.IGasStorageCell;
import extracells.api.IHandlerFluidStorage;
import extracells.registries.ItemEnum;
import extracells.util.inventory.ECFluidFilterInventory;
import extracells.util.inventory.ECPrivateInventory;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

import java.util.ArrayList;
import java.util.List;

public class ItemStorageGas extends ItemECBase implements IGasStorageCell {

	public static final String[] suffixes = { "1k", "4k", "16k", "64k", "256k", "1024k", "4096k" };

	public static final int[] spaces = { 1024, 4096, 16348, 65536, 262144, 1048576, 4194304 };

	private IIcon[] icons;

	public ItemStorageGas() {
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player,
			List list, boolean par4) {
		IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS);
		if (!(handler instanceof IHandlerFluidStorage)) {
			return;
		}
		IHandlerFluidStorage cellHandler = (IHandlerFluidStorage) handler;
		boolean partitioned = cellHandler.isFormatted();
		long usedBytes = cellHandler.usedBytes();

		list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.gas.bytes"), usedBytes / 250, cellHandler.totalBytes() / 250));
		list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.gas.types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
		if (usedBytes != 0) {
			list.add(String.format(StatCollector.translateToLocal("extracells.tooltip.storage.gas.content"), usedBytes));
		}

		if (partitioned) {
			list.add(StatCollector.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + StatCollector.translateToLocal("gui.appliedenergistics2.Precise"));
		}
	}

	@Override
	public IInventory getConfigInventory(ItemStack is) {
		return new ECFluidFilterInventory("configFluidCell", 63, is);
	}

	@Override
	public ArrayList<Fluid> getFilter(ItemStack stack) {
		ECFluidFilterInventory inventory = new ECFluidFilterInventory("", 63, stack);
		ItemStack[] stacks = inventory.slots;
		ArrayList<Fluid> filter = new ArrayList<Fluid>();
		if (stacks.length == 0)
			return null;
		for (ItemStack s : stacks) {
			if (s == null)
				continue;
			Fluid f = FluidRegistry.getFluid(s.getItemDamage());
			if (f != null)
				filter.add(f);
		}
		return filter;
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is) {
		if (is == null)
			return null;
		if (!is.hasTagCompound())
			is.setTagCompound(new NBTTagCompound());
		if (is.getTagCompound().hasKey("fuzzyMode"))
			return FuzzyMode.valueOf(is.getTagCompound().getString("fuzzyMode"));
		is.getTagCompound().setString("fuzzyMode", FuzzyMode.IGNORE_ALL.name());
		return FuzzyMode.IGNORE_ALL;
	}

	@Override
	public IIcon getIconFromDamage(int dmg) {
		int j = MathHelper.clamp_int(dmg, 0, suffixes.length);
		return this.icons[j];
	}

	@Override
	public int getMaxBytes(ItemStack is) {
		return spaces[Math.max(0, is.getItemDamage())];
	}

	@Override
	public int getMaxTypes(ItemStack unused) {
		return 5;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void getSubItems(Item item, CreativeTabs creativeTab,
			List listSubItems) {
		for (int i = 0; i < suffixes.length; ++i) {
			listSubItems.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.storage.gas." + suffixes[itemStack.getItemDamage()];
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is) {
		return new ECPrivateInventory("configInventory", 0, 64);
	}

	@Override
	public boolean isEditable(ItemStack is) {
		if (is == null)
			return false;
		return is.getItem() == this;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world,
			EntityPlayer entityPlayer) {
		if (!entityPlayer.isSneaking()) {
			return itemStack;
		}
		IMEInventoryHandler<IAEFluidStack> handler = AEApi.instance().registries().cell().getCellInventory(itemStack, null, StorageChannel.FLUIDS);
		if (!(handler instanceof IHandlerFluidStorage)) {
			return itemStack;
		}
		IHandlerFluidStorage cellHandler = (IHandlerFluidStorage) handler;
		if (cellHandler.usedBytes() == 0 && entityPlayer.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(2))) {
			return ItemEnum.STORAGECOMPONENT.getDamagedStack(itemStack.getItemDamage() + 11);
		}
		return itemStack;
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icons = new IIcon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i) {
			this.icons[i] = iconRegister.registerIcon("extracells:" + "storage.gas." + suffixes[i]);
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		if (is == null)
			return;
		NBTTagCompound tag;
		if (is.hasTagCompound())
			tag = is.getTagCompound();
		else
			tag = new NBTTagCompound();
		tag.setString("fuzzyMode", fzMode.name());
		is.setTagCompound(tag);

	}

	@Override
	public String getOreFilter(ItemStack itemStack) {
		return "";
	}

	@Override
	public void setOreFilter(ItemStack itemStack, String s) {

	}
}
