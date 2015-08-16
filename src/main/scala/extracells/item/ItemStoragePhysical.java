package extracells.item;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.*;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import cofh.api.energy.IEnergyContainerItem;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import extracells.Extracells;
import extracells.registries.ItemEnum;
import extracells.util.inventory.ECCellInventory;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.List;

@Optional.Interface(iface = "cofh.api.energy.IEnergyContainerItem", modid = "CoFHAPI|energy")
public class ItemStoragePhysical extends Item implements IStorageCell,
		IAEItemPowerStorage, IEnergyContainerItem {

	public static final String[] suffixes = { "256k", "1024k", "4096k", "16384k", "container" };

	public static final int[] bytes_cell = { 262144, 1048576, 4194304, 16777216, 65536 };
	public static final int[] types_cell = { 63, 63, 63, 63, 1 };
	private IIcon[] icons;
	private final int MAX_POWER = 32000;

	public ItemStoragePhysical() {
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player,
			List list, boolean par4) {
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		IMEInventoryHandler<IAEItemStack> invHandler = cellRegistry
				.getCellInventory(itemStack, null, StorageChannel.ITEMS);
		ICellInventoryHandler inventoryHandler = (ICellInventoryHandler) invHandler;
		ICellInventory cellInv = inventoryHandler.getCellInv();
		long usedBytes = cellInv.getUsedBytes();

		list.add(String.format(StatCollector
				.translateToLocal("extracells.tooltip.storage.physical.bytes"),
				usedBytes, cellInv.getTotalBytes()));
		list.add(String.format(StatCollector
				.translateToLocal("extracells.tooltip.storage.physical.types"),
				cellInv.getStoredItemTypes(), cellInv.getTotalItemTypes()));
		if (usedBytes > 0)
			list.add(String.format(
					StatCollector
							.translateToLocal("extracells.tooltip.storage.physical.content"),
					cellInv.getStoredItemCount()));
	}

	@Override
	public int getBytesPerType(ItemStack cellItem) {
		return Extracells.dynamicTypes() ? bytes_cell[MathHelper.clamp_int(
				cellItem.getItemDamage(), 0, suffixes.length - 1)] / 128 : 8;
	}

	@Override
	@Deprecated
	public int BytePerType(ItemStack cellItem) {
		return getBytesPerType(cellItem);
	}

	private NBTTagCompound ensureTagCompound(ItemStack itemStack) {
		if (!itemStack.hasTagCompound())
			itemStack.setTagCompound(new NBTTagCompound());
		return itemStack.getTagCompound();
	}

	@Override
	public double extractAEPower(ItemStack itemStack, double amt) {
		if (itemStack == null || itemStack.getItemDamage() != 4)
			return 0.0D;
		NBTTagCompound tagCompound = ensureTagCompound(itemStack);
		double currentPower = tagCompound.getDouble("power");
		double toExtract = Math.min(amt, currentPower);
		tagCompound.setDouble("power", currentPower - toExtract);
		return toExtract;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int extractEnergy(ItemStack container, int maxExtract,
			boolean simulate) {
		if (container == null || container.getItemDamage() != 4)
			return 0;
		if (simulate) {
			return getEnergyStored(container) >= maxExtract ? maxExtract
					: getEnergyStored(container);
		} else {
			return (int) PowerUnits.AE
					.convertTo(
							PowerUnits.RF,
							extractAEPower(container, PowerUnits.RF.convertTo(
									PowerUnits.AE, maxExtract)));
		}
	}

	@Override
	public double getAECurrentPower(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItemDamage() != 4)
			return 0.0D;
		NBTTagCompound tagCompound = ensureTagCompound(itemStack);
		return tagCompound.getDouble("power");
	}

	@Override
	public double getAEMaxPower(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItemDamage() != 4)
			return 0.0D;
		return this.MAX_POWER;
	}

	@Override
	public int getBytes(ItemStack cellItem) {
		return bytes_cell[MathHelper.clamp_int(cellItem.getItemDamage(), 0,
				suffixes.length - 1)];
	}

	@Override
	public IInventory getConfigInventory(ItemStack is) {
		return new ECCellInventory(is, "config", 63, 1);
	}

	@Override
	public double getDurabilityForDisplay(ItemStack itemStack) {
		if (itemStack == null || itemStack.getItemDamage() != 4)
			return super.getDurabilityForDisplay(itemStack);
		return 1 - getAECurrentPower(itemStack) / this.MAX_POWER;
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getEnergyStored(ItemStack arg0) {
		return (int) PowerUnits.AE.convertTo(PowerUnits.RF,
				getAECurrentPower(arg0));
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is) {
		if (!is.hasTagCompound())
			is.setTagCompound(new NBTTagCompound());
		return FuzzyMode.values()[is.getTagCompound().getInteger("fuzzyMode")];
	}

	@Override
	public IIcon getIconFromDamage(int dmg) {
		return this.icons[MathHelper.clamp_int(dmg, 0, suffixes.length - 1)];
	}

	@Override
	public double getIdleDrain() {
		return 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		if (stack == null)
			return super.getItemStackDisplayName(stack);
		if (stack.getItemDamage() == 4) {
			try {
				IItemList list = AEApi
						.instance()
						.registries()
						.cell()
						.getCellInventory(stack, null, StorageChannel.ITEMS)
						.getAvailableItems(
								AEApi.instance().storage().createItemList());
				if (list.isEmpty())
					return super.getItemStackDisplayName(stack)
							+ " - "
							+ StatCollector
									.translateToLocal("extracells.tooltip.empty1");
				IAEItemStack s = (IAEItemStack) list.getFirstItem();
				return super.getItemStackDisplayName(stack) + " - "
						+ s.getItemStack().getDisplayName();
			} catch (Throwable e) {}
			return super.getItemStackDisplayName(stack)
					+ " - "
					+ StatCollector
							.translateToLocal("extracells.tooltip.empty1");
		}
		return super.getItemStackDisplayName(stack);
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int getMaxEnergyStored(ItemStack arg0) {
		return (int) PowerUnits.AE
				.convertTo(PowerUnits.RF, getAEMaxPower(arg0));
	}

	@Override
	public AccessRestriction getPowerFlow(ItemStack itemStack) {
		if (itemStack == null)
			return null;
		return itemStack.getItemDamage() == 4 ? AccessRestriction.READ_WRITE
				: AccessRestriction.NO_ACCESS;
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.epic;
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs creativeTab, List itemList) {
		for (int i = 0; i < suffixes.length; i++) {
			itemList.add(new ItemStack(item, 1, i));
			if (i == 4) {
				ItemStack s = new ItemStack(item, 1, i);
				s.setTagCompound(new NBTTagCompound());
				s.getTagCompound().setDouble("power", this.MAX_POWER);
				itemList.add(s);
			}
		}
	}

	@Override
	public int getTotalTypes(ItemStack cellItem) {
		return types_cell[MathHelper.clamp_int(cellItem.getItemDamage(), 0,
				suffixes.length - 1)];
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		return "extracells.item.storage.physical."
				+ suffixes[itemStack.getItemDamage()];
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is) {
		return new ECCellInventory(is, "upgrades", 2, 1);
	}

	@Override
	public double injectAEPower(ItemStack itemStack, double amt) {
		if (itemStack == null || itemStack.getItemDamage() != 4)
			return 0.0D;
		NBTTagCompound tagCompound = ensureTagCompound(itemStack);
		double currentPower = tagCompound.getDouble("power");
		double toInject = Math.min(amt, this.MAX_POWER - currentPower);
		tagCompound.setDouble("power", currentPower + toInject);
		return toInject;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem,
			IAEItemStack requestedAddition) {
		return false;
	}

	@Override
	public boolean isEditable(ItemStack is) {
		return true;
	}

	@Override
	public boolean isStorageCell(ItemStack i) {
		return true;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ItemStack onItemRightClick(ItemStack itemStack, World world,
			EntityPlayer entityPlayer) {
		if (itemStack == null)
			return itemStack;
		if (itemStack.getItemDamage() == 4 && !world.isRemote
				&& entityPlayer.isSneaking()) {
			switch (itemStack.getTagCompound().getInteger("mode")) {
			case 0:
				itemStack.getTagCompound().setInteger("mode", 1);
				entityPlayer.addChatMessage(new ChatComponentTranslation(
						"extracells.tooltip.storage.container.1"));
				break;
			case 1:
				itemStack.getTagCompound().setInteger("mode", 2);
				entityPlayer.addChatMessage(new ChatComponentTranslation(
						"extracells.tooltip.storage.container.2"));
				break;
			case 2:
				itemStack.getTagCompound().setInteger("mode", 0);
				entityPlayer.addChatMessage(new ChatComponentTranslation(
						"extracells.tooltip.storage.container.0"));
				break;
			}
			return itemStack;
		}
		if (!entityPlayer.isSneaking())
			return itemStack;
		IMEInventoryHandler<IAEItemStack> invHandler = AEApi.instance()
				.registries().cell()
				.getCellInventory(itemStack, null, StorageChannel.ITEMS);
		ICellInventoryHandler inventoryHandler = (ICellInventoryHandler) invHandler;
		ICellInventory cellInv = inventoryHandler.getCellInv();
		if (cellInv.getUsedBytes() == 0
				&& entityPlayer.inventory
						.addItemStackToInventory(ItemEnum.STORAGECASING
								.getDamagedStack(0)))
			return ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack
					.getItemDamage());
		return itemStack;
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player,
			World world, int x, int y, int z, int side, float xOffset,
			float yOffset, float zOffset) {
		if (itemstack == null || player == null)
			return false;
		if (itemstack.getItemDamage() == 4 && !player.isSneaking()) {
			double power = getAECurrentPower(itemstack);
			ForgeDirection face = ForgeDirection.getOrientation(side);
			IItemList list = AEApi
					.instance()
					.registries()
					.cell()
					.getCellInventory(itemstack, null, StorageChannel.ITEMS)
					.getAvailableItems(
							AEApi.instance().storage().createItemList());
			if (list.isEmpty())
				return false;
			IAEItemStack storageStack = (IAEItemStack) list.getFirstItem();
			if (world.getBlock(x + face.offsetX, y + face.offsetY, z
					+ face.offsetZ) == Blocks.air
					&& storageStack.getStackSize() != 0 && power >= 20.0D) {
				if (!world.isRemote) {
					IAEItemStack request = storageStack.copy();
					request.setStackSize(1);
					ItemStack block = request.getItemStack();
					if (block.getItem() instanceof ItemBlock) {
						ItemBlock itemblock = (ItemBlock) request.getItem();
						if (world.getBlock(x, y, z) != Blocks.bedrock && world.getBlock(x, y, z).getBlockHardness(world, x, y, z) >= 0.0F) {
							switch (itemstack.getTagCompound().getInteger(
									"mode")) {
							case 0:
								request.setStackSize(1);
								itemblock.onItemUseFirst(
										request.getItemStack(), player, world,
										x, y, z, side, xOffset, yOffset,
										zOffset);
								itemblock.onItemUse(request.getItemStack(),
										player, world, x, y, z, side, xOffset,
										yOffset, zOffset);
								AEApi.instance()
										.registries()
										.cell()
										.getCellInventory(itemstack, null,
												StorageChannel.ITEMS)
										.extractItems(request,
												Actionable.MODULATE,
												new PlayerSource(player, null));
								extractAEPower(player.getCurrentEquippedItem(),
										20.0D);
								break;
							case 1:
								request.setStackSize(1);
								world.func_147480_a(x, y, z, true);
								placeBlock(request.getItemStack(), world,
										player, x, y, z, side, xOffset,
										yOffset, zOffset);
								AEApi.instance()
										.registries()
										.cell()
										.getCellInventory(itemstack, null,
												StorageChannel.ITEMS)
										.extractItems(request,
												Actionable.MODULATE,
												new PlayerSource(player, null));
								break;
							case 2:

								request.setStackSize(9);
								if (storageStack.getStackSize() > 9
										&& power >= 180.0D) {
									switch (ForgeDirection.getOrientation(side)) {
									case DOWN:
										for (int posX = x - 1; posX < x + 2; posX++) {
											for (int posZ = z - 1; posZ < z + 2; posZ++) {
												world.func_147480_a(posX, y,
														posZ, true);
												placeBlock(
														request.getItemStack(),
														world, player, x, y, z,
														side, xOffset, yOffset,
														zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									case EAST:
										for (int posZ = z - 1; posZ < z + 2; posZ++) {
											for (int posY = y - 1; posY < y + 2; posY++) {
												world.func_147480_a(x, posY,
														posZ, true);
												placeBlock(
														request.getItemStack(),
														world, player, x, posY,
														posZ, side, xOffset,
														yOffset, zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									case NORTH:
										for (int posX = x - 1; posX < x + 2; posX++) {
											for (int posY = y - 1; posY < y + 2; posY++) {
												world.func_147480_a(posX, posY,
														z, true);
												placeBlock(
														request.getItemStack(),
														world, player, posX,
														posY, z, side, xOffset,
														yOffset, zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									case SOUTH:
										for (int posX = x - 1; posX < x + 2; posX++) {
											for (int posY = y - 1; posY < y + 2; posY++) {
												world.func_147480_a(posX, posY,
														z, true);
												placeBlock(
														request.getItemStack(),
														world, player, posX,
														posY, z, side, xOffset,
														yOffset, zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									case UNKNOWN:
										break;
									case UP:
										for (int posX = x - 1; posX < x + 2; posX++) {
											for (int posZ = z - 1; posZ < z + 2; posZ++) {
												world.func_147480_a(posX, y,
														posZ, true);
												placeBlock(
														request.getItemStack(),
														world, player, posX, y,
														posZ, side, xOffset,
														yOffset, zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									case WEST:
										for (int posZ = z - 1; posZ < z + 2; posZ++) {
											for (int posY = y - 1; posY < y + 2; posY++) {
												world.func_147480_a(x, posY,
														posZ, true);
												placeBlock(
														request.getItemStack(),
														world, player, x, posY,
														posZ, side, xOffset,
														yOffset, zOffset);
											}
										}
										AEApi.instance()
												.registries()
												.cell()
												.getCellInventory(itemstack,
														null,
														StorageChannel.ITEMS)
												.extractItems(
														request,
														Actionable.MODULATE,
														new PlayerSource(
																player, null));
										break;
									default:
										break;
									}
								}
							}
							return true;
						} else {
							return false;
						}
					} else {
						player.addChatMessage(new ChatComponentTranslation(
								"extracells.tooltip.onlyblocks"));
						return false;
					}
				} else {
					return false;
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public void placeBlock(ItemStack itemstack, World world,
			EntityPlayer player, int x, int y, int z, int side, float xOffset,
			float yOffset, float zOffset) {
		extractAEPower(player.getCurrentEquippedItem(), 20.0D);
		ItemBlock itemblock = (ItemBlock) itemstack.getItem();
		switch (ForgeDirection.getOrientation(side)) {
		case DOWN:
			itemblock.onItemUseFirst(itemstack, player, world, x, y++, z, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y++, z, side,
					xOffset, yOffset, zOffset);
			break;
		case EAST:
			itemblock.onItemUseFirst(itemstack, player, world, x--, y, z, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x--, y, z, side,
					xOffset, yOffset, zOffset);
			break;
		case NORTH:
			itemblock.onItemUseFirst(itemstack, player, world, x, y, z++, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y, z++, side,
					xOffset, yOffset, zOffset);
			break;
		case SOUTH:
			itemblock.onItemUseFirst(itemstack, player, world, x, y, z--, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y, z--, side,
					xOffset, yOffset, zOffset);
			break;
		case UNKNOWN:
			break;
		case UP:
			itemblock.onItemUseFirst(itemstack, player, world, x, y--, z, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x, y--, z, side,
					xOffset, yOffset, zOffset);
			break;
		case WEST:
			itemblock.onItemUseFirst(itemstack, player, world, x++, y, z, side,
					xOffset, yOffset, zOffset);
			itemblock.onItemUse(itemstack, player, world, x++, y, z, side,
					xOffset, yOffset, zOffset);
			break;
		default:
			break;
		}
	}

	@Override
	@Optional.Method(modid = "CoFHAPI|energy")
	public int receiveEnergy(ItemStack container, int maxReceive,
			boolean simulate) {
		if (container == null || container.getItemDamage() != 4)
			return 0;
		if (simulate) {
			double current = PowerUnits.AE.convertTo(PowerUnits.RF,
					getAECurrentPower(container));
			double max = PowerUnits.AE.convertTo(PowerUnits.RF,
					getAEMaxPower(container));
			if (max - current >= maxReceive)
				return maxReceive;
			else
				return (int) (max - current);
		} else {
			int notStored = (int) PowerUnits.AE
					.convertTo(
							PowerUnits.RF,
							injectAEPower(container, PowerUnits.RF.convertTo(
									PowerUnits.AE, maxReceive)));
			return maxReceive - notStored;
		}
	}

	@Override
	public void registerIcons(IIconRegister iconRegister) {
		this.icons = new IIcon[suffixes.length];

		for (int i = 0; i < suffixes.length; ++i) {
			this.icons[i] = iconRegister.registerIcon("extracells:"
					+ "storage.physical." + suffixes[i]);
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
		if (!is.hasTagCompound())
			is.setTagCompound(new NBTTagCompound());
		is.getTagCompound().setInteger("fuzzyMode", fzMode.ordinal());
	}

	@Override
	public boolean showDurabilityBar(ItemStack itemStack) {
		if (itemStack == null)
			return false;
		return itemStack.getItemDamage() == 4 ? true : false;
	}

	@Override
	public boolean storableInStorageCell() {
		return false;
	}
}
