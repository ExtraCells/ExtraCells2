package extracells.item.storage;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import appeng.api.AEApi;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.ICellWorkbenchItem;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEFluidStack;
import extracells.api.IHandlerFluidStorage;
import extracells.item.ItemECBase;
import extracells.models.ModelManager;
import extracells.registries.ItemEnum;

public abstract class ItemStorageCell extends ItemECBase implements ICellWorkbenchItem {
	protected final CellDefinition definition;
	protected final StorageChannel channel;

	public ItemStorageCell(CellDefinition definition, StorageChannel channel) {
		this.definition = definition;
		this.channel = channel;
		setMaxStackSize(1);
		setMaxDamage(0);
		setHasSubtypes(true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advanced) {
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		IMEInventoryHandler<IAEFluidStack> handler = cellRegistry.getCellInventory(itemStack, null, channel);
		if (!(handler instanceof IHandlerFluidStorage)) {
			return;
		}
		IHandlerFluidStorage cellHandler = (IHandlerFluidStorage) handler;
		boolean partitioned = cellHandler.isFormatted();
		long usedBytes = cellHandler.usedBytes();

		list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".bytes"), usedBytes / 250, cellHandler.totalBytes() / 250));
		list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".types"), cellHandler.usedTypes(), cellHandler.totalTypes()));
		if (usedBytes != 0) {
			list.add(String.format(I18n.translateToLocal("extracells.tooltip.storage." + definition + ".content"), usedBytes));
		}

		if (partitioned) {
			list.add(I18n.translateToLocal("gui.appliedenergistics2.Partitioned") + " - " + I18n.translateToLocal("gui.appliedenergistics2.Precise"));
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack itemStack) {
		return EnumRarity.RARE;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs creativeTab, List listSubItems) {
		for (StorageType type : definition.cells) {
			listSubItems.add(new ItemStack(item, 1, type.getMeta()));
		}
	}

	@Override
	public String getUnlocalizedName(ItemStack itemStack) {
		StorageType type = definition.cells.fromMeta(itemStack.getItemDamage());
		return "extracells.item.storage." + type.getIdentifier();
	}

	@Override
	public boolean isEditable(ItemStack itemStack) {
		if (itemStack == null) {
			return false;
		}
		return itemStack.getItem() == this;
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(ItemStack itemStack, World world, EntityPlayer player, EnumHand hand) {
		if (!player.isSneaking()) {
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
		}
		ICellRegistry cellRegistry = AEApi.instance().registries().cell();
		IMEInventoryHandler<IAEFluidStack> handler = cellRegistry.getCellInventory(itemStack, null, channel);
		if (!(handler instanceof IHandlerFluidStorage)) {
			return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
		}
		IHandlerFluidStorage cellHandler = (IHandlerFluidStorage) handler;
		if (cellHandler.usedBytes() == 0 && player.inventory.addItemStackToInventory(ItemEnum.STORAGECASING.getDamagedStack(definition.ordinal()))) {
			return new ActionResult<>(EnumActionResult.SUCCESS, ItemEnum.STORAGECOMPONET.getDamagedStack(itemStack.getItemDamage() + definition.componentMetaStart));
		}
		return new ActionResult<>(EnumActionResult.SUCCESS, itemStack);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel(Item item, ModelManager manager) {
		for (StorageType type : definition.cells) {
			manager.registerItemModel(item, type.getMeta(), type.getModelName());
		}
	}
}
